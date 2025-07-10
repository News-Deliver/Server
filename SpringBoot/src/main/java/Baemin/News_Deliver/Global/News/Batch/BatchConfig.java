package Baemin.News_Deliver.Global.News.Batch;

import Baemin.News_Deliver.Global.News.Batch.JPAINSERT.dto.NewsItemDTO;
import Baemin.News_Deliver.Global.News.Batch.JPAINSERT.entity.News;
import Baemin.News_Deliver.Global.News.Batch.JPAINSERT.service.BatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class BatchConfig {

    @Bean
    public Job newsDataSaveJob(JobRepository jobRepository, Step newsDataSaveStep) {
        return new JobBuilder("newsDataSaveJob", jobRepository)
                .start(newsDataSaveStep)
                .build();
    }

    @Bean
    public Step newsDataSaveStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 ItemReader<NewsItemDTO> apiReader,
                                 ItemProcessor<NewsItemDTO, News> newsProcessor,
                                 ItemWriter<News> newsWriter) {

        return new StepBuilder("newsDataSaveStep", jobRepository)
                .<NewsItemDTO, News>chunk(10, transactionManager)  // 10개씩 처리
                .reader(apiReader)
                .processor(newsProcessor)
                .writer(newsWriter)
                .build();
    }

    @Bean
    public ItemReader<NewsItemDTO> apiReader() {
        // API에서 데이터를 모두 불러와서 ListItemReader로 반환
        // ListItemReader가 이 데이터를 하나씩 읽어서 다음 단계로 전달

        //예시) 이렇게 리스트를 ListItemReader로 변환해야함.
        //List<NewsItemDTO> newsList = BatchService.fetchNews();
        //return new ListItemReader<>(newsList);

        //배치 코드 넣기

        return null;
    }

    @Bean
    public ItemProcessor<NewsItemDTO, News> newsProcessor() {
        // 받아온 DTO를 엔티티로 변환하는 과정 (하나씩 처리)

        //예시) 이렇게 dto를 뉴스 엔티티로 변환과정 필요함.
        //return dto -> new News(dto.getTitle(), dto.getUrl());

        //배치 코드 넣기

        return null;
    }

    @Bean
    public ItemWriter<News> newsWriter() {
        // Chunk 단위로 모아진 News 엔티티들을 한번에 DB에 저장
        //return items -> newsRepository.saveAll(items);

        //jdbc로 바꿀 예정임.
        //배치코드 넣기

        return null;
    }
}
