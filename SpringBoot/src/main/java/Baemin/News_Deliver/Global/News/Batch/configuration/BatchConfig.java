package Baemin.News_Deliver.Global.News.Batch.configuration;

import Baemin.News_Deliver.Global.News.Batch.listener.BatchJobCompletionListener;
import Baemin.News_Deliver.Global.News.JPAINSERT.dto.NewsItemDTO;
import Baemin.News_Deliver.Global.News.JPAINSERT.dto.NewsResponseDTO;
import Baemin.News_Deliver.Global.News.JPAINSERT.entity.News;
import Baemin.News_Deliver.Global.News.JPAINSERT.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class BatchConfig {

    @Value("${deepsearch.api.key}")
    private String apiKey;
    private static final String API_URL = "https://api-v2.deepsearch.com/v1/articles";

    @Bean
    public Job newsDataSaveJob(JobRepository jobRepository,
                               Step newsDataSaveStep,
                               BatchJobCompletionListener listener) {
        return new JobBuilder("newsDataSaveJob", jobRepository)
                .start(newsDataSaveStep)
                .listener(listener)
                .build();
    }

    @Bean
    public Step newsDataSaveStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 ItemReader<NewsItemDTO> apiReader,
                                 ItemProcessor<NewsItemDTO, News> newsProcessor,
                                 ItemWriter<News> newsWriter) {

        return new StepBuilder("newsDataSaveStep", jobRepository)
                .<NewsItemDTO, News>chunk(10_000, transactionManager)  // 10000개씩 처리
                .reader(apiReader)
                .processor(newsProcessor)
                .writer(newsWriter)
                .build();
    }

    @StepScope
    @Bean
    public ItemReader<NewsItemDTO> apiReader(@Value("#{jobParameters['section']}") String section) {
        // API에서 데이터를 모두 불러와서 ListItemReader로 반환
        // ListItemReader가 이 데이터를 하나씩 읽어서 다음 단계로 전달

        //하루 전날 날짜 구하기
        LocalDate day = LocalDate.now().minusDays(1);

        //원하는 포맷 지정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        //문자열로 변환
        String dateTo = day.format(formatter);
        String dateFrom = day.format(formatter);

        int pageSize = 100;

        log.info("🔍 검색한 기간: {} ~ {}", dateFrom, dateTo);

        // 먼저 page=1 호출 → total_pages 얻기
        int totalPages = getPage(1, section, pageSize, dateFrom, dateTo);
        List<NewsItemDTO> newsList = new ArrayList<>();

        for (int page = 1; page <= totalPages; page++) {
            getNewsList(page, section, pageSize, dateFrom, dateTo, newsList);
            if (newsList == null) {
                log.warn("⚠️ [{}] Page {} 수집 실패 또는 빈 응답", section, page);
            }
        }

        log.info("📦 [{}] 전체 뉴스 수: {}", section, newsList.size());
        return new ListItemReader<>(newsList);
    }


    @Bean
    public ItemProcessor<NewsItemDTO, News> newsProcessor() {
        // 받아온 DTO를 엔티티로 변환하는 과정 (하나씩 처리)

        //예시) 이렇게 dto를 뉴스 엔티티로 변환과정 필요함.
        //return dto -> new News(dto.getTitle(), dto.getUrl());
        return dto -> {
            if (dto.getSections() == null || dto.getSections().isEmpty()) {
                log.warn("❌ 섹션 정보 없음 → 건너뜀 (title: {})", dto.getTitle());
                return null; // sections가 없으면 skip
            }

            return News.builder()
                    .title(dto.getTitle())
                    .summary(dto.getSummary())
                    .publisher(dto.getPublisher())
                    .contentUrl(dto.getContent_url())
                    .publishedAt(dto.getPublished_at())
                    .sections(dto.getSections().get(0)) // List → String
                    .send(false)
                    .build();
        };
    }

    @Bean
    public ItemWriter<News> newsWriter(javax.sql.DataSource dataSource) {
        JdbcBatchItemWriter<News> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setSql("""
        INSERT INTO news (title, summary, content_url, published_at, send, sections, publisher)
        VALUES (:title, :summary, :contentUrl, :publishedAt, :send, :sections, :publisher)
    """);

        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.afterPropertiesSet(); // 설정 검증 필수

        return writer;
    }

    private void getNewsList(int page, String section, int pageSize, String dateFrom, String dateTo, List<NewsItemDTO> newsList) {
        RestTemplate restTemplate = new RestTemplate();

        // 쿼리 파라미터 구성
        String url = UriComponentsBuilder.fromHttpUrl(API_URL)
                .pathSegment(section)
                .queryParam("page_size", pageSize)
                .queryParam("date_to", dateTo)
                .queryParam("date_from", dateFrom)
                .queryParam("order", "published_at")
                .queryParam("page", page)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<NewsResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                NewsResponseDTO.class
        );

        NewsResponseDTO body = response.getBody();

        if (body != null && body.getData() != null) {
            for (NewsItemDTO item : body.getData()) {
                newsList.add(item);
            }
        }

    }

    private int getPage(int page, String section, int pageSize, String dateFrom, String dateTo) {
        RestTemplate restTemplate = new RestTemplate();

        // 쿼리 파라미터 구성
        String url = UriComponentsBuilder.fromHttpUrl(API_URL)
                .pathSegment(section)
                .queryParam("page_size", pageSize)
                .queryParam("date_to", dateTo)
                .queryParam("date_from", dateFrom)
                .queryParam("order", "published_at")
                .queryParam("page", page)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<NewsResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                NewsResponseDTO.class
        );

        return response.getBody().getTotal_pages();
    }
}
