package Baemin.News_Deliver.Global.NewsMonitoring.Config;

import Baemin.News_Deliver.Global.News.Batch.dto.NewsItemDTO;
import Baemin.News_Deliver.Global.News.Batch.entity.News;
import Baemin.News_Deliver.Global.NewsMonitoring.Manager.NewsMonitoringManager;
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
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class NewsMonitoringConfig {

    private final NewsMonitoringManager newsMonitoringManager;

    /**
     * 뉴스 데이터를 저장하는 Spring Batch Job을 정의하는 메서드
     *
     * DB에 저장하는 단일 Step(`newsDataSaveStep`)으로 구성
     *
     * @param jobRepository Spring Batch의 JobRepository로, Job의 실행 정보(인스턴스, 실행 상태 등)를 저장하고 관리
     * @param newsDataSaveStep_Monitoring 뉴스 데이터를 수집하고 저장하는 Step 객체
     * @return 구성된 Job 객체 (newsDataSaveJob)
     */
    @Bean
    public Job newsDataSaveJob_Monitoring(JobRepository jobRepository, Step newsDataSaveStep_Monitoring) {
        return new JobBuilder("newsDataSaveJob_Monitoring", jobRepository)
                .start(newsDataSaveStep_Monitoring)
                .build();
    }

    /**
     * 뉴스 데이터를 저장하는 Step을 정의하는 메서드
     *
     * chunk 단위는 10,000개로 설정되어 있어, 10,000개씩 트랜잭션 단위로 처리 및 커밋
     *
     * @param jobRepository Spring Batch의 JobRepository로, Step 실행 정보를 관리하는 메타 저장소
     * @param transactionManager 트랜잭션 경계를 관리하는 트랜잭션 매니저, chunk 단위 커밋에 사용
     * @param apiReader_Monitoring 뉴스 데이터를 외부 API로부터 읽어오는 ItemReader
     * @param newsProcessor_Monitoring NewsItemDTO를 News 엔티티로 변환하는 ItemProcessor
     * @param newsWriter_Monitoring 변환된 News 엔티티를 DB에 저장하는 ItemWriter
     * @return 구성된 Step 객체 (newsDataSaveStep)
     */
    @Bean
    public Step newsDataSaveStep_Monitoring(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 ItemReader<NewsItemDTO> apiReader_Monitoring,
                                 ItemProcessor<NewsItemDTO, News> newsProcessor_Monitoring,
                                 ItemWriter<News> newsWriter_Monitoring) {

        return new StepBuilder("newsDataSaveStep", jobRepository)
                .<NewsItemDTO, News>chunk(10_000, transactionManager)  // 10000개씩 처리
                .reader(apiReader_Monitoring)
                .processor(newsProcessor_Monitoring)
                .writer(newsWriter_Monitoring)
                .build();
    }

    /**
     * 외부 뉴스 API에서 데이터를 읽어오는 ItemReader 생성 메서드
     *
     * @param section Job 파라미터로 전달되는 뉴스 섹션 이름 (예: "politics", "economy" 등)
     * @return 지정된 섹션의 뉴스 데이터를 순차적으로 읽는 ItemReader
     */
    @StepScope
    @Bean
    public ItemReader<NewsItemDTO> apiReader_Monitoring(@Value("#{jobParameters['section']}") String section) {

        /* 오늘의 날짜 확인 */
        LocalDate day = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        /* 날짜를 문자열로 변환 */
        String dateTo = day.format(formatter);
        String dateFrom = day.format(formatter);

        /* Batch를 요청할 페이지 수 계산(한 페이지에 100개씩 호출) */
        int totalItem = newsMonitoringManager.getTotalItems(section,dateFrom,dateTo);
        int requestPageSize = totalItem/100;
        if(requestPageSize>=100) requestPageSize = 100; // Total Page 요청이 100이 넘으면 100으로 통일
        log.info("requestPageSize(요청할 페이지 수) : {} ",requestPageSize);

        /* DeepSearch API를 통한 데이터 수집 메서드 호출 */
        List<NewsItemDTO> newsList = newsMonitoringManager.fetchAllPages(section,dateFrom,dateTo,requestPageSize);
        log.info("newsList.size() : {}",newsList.size());

        /* ListItemReader로 이 데이터를 하나씩 읽어서 다음 단계로 전달 */
        return new ListItemReader<>(newsList);
    }

    /**
     * DTO를 Entity(DB에 저장할 수 있는)로 변환하는 메서드
     *
     * @return NewsItemDTO를 News로 변환하는 ItemProcessor
     */
    @Bean
    public ItemProcessor<NewsItemDTO, News> newsProcessor_Monitoring() {

        /* sections가 없으면 skip */
        return dto -> {
            if (dto.getSections() == null || dto.getSections().isEmpty()) {
                log.info("섹션 정보 없음 → 건너뜀 (title: {})", dto.getTitle());
                return null;
            }

            /* 이전 단계에서 받은 DTO -> Entity 변환*/
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

    /**
     * 변환된 News 엔티티를 DB에 저장하는 ItemWriter(Jdbc 기반)
     *
     * @param dataSource Spring이 관리하는 DataSource 객체로, DB 커넥션을 제공하는 주입 대상
     * @return News 엔티티 리스트를 DB에 저장하는 ItemWriter
     */
    @Bean
    public ItemWriter<News> newsWriter_Monitoring(javax.sql.DataSource dataSource) {


        /* 실제 DB 저장 로직 */
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
}