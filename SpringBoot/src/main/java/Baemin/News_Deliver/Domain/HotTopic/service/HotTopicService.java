package Baemin.News_Deliver.Domain.HotTopic.service;

import Baemin.News_Deliver.Domain.HotTopic.dto.HotTopicResponseDTO;
import Baemin.News_Deliver.Domain.HotTopic.entity.HotTopic;
import Baemin.News_Deliver.Domain.HotTopic.repository.HotTopicRepository;
import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import Baemin.News_Deliver.Global.News.ElasticSearch.service.NewsEsService;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotTopicService {

    private final ElasticsearchClient elasticsearchClient;
    private final HotTopicRepository hotTopicRepository;
    private final NewsEsService elasticSearchService;

    //ElasticSearch에서 "어제"의 핫토픽 추출 > 반환
    public List<HotTopicResponseDTO> getHotTopicList() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfYesterday = yesterday.atStartOfDay(); // 어제 00:00:00
        LocalDateTime endOfYesterday = yesterday.atTime(LocalTime.MAX); // 어제 23:59:59.999999999

        return hotTopicRepository.findTop10ByTopicDateBetweenOrderByTopicRankAsc(startOfYesterday, endOfYesterday).stream()
                .map(entity -> HotTopicResponseDTO.builder()
                        .topicRank(entity.getTopicRank())
                        .keyword(entity.getKeyword())
                        .keywordCount(entity.getKeywordCount())
                        .topicDate(entity.getTopicDate())
                        .build())
                .toList();
    }

    //ElasticSearch에서 "어제"의 핫토픽 추출 > DB에 저장
    @Transactional
    public void getAndSaveHotTopic() throws IOException {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        List<StringTermsBucket> buckets = elasticSearchService.getTopKeywordsForDateRange(
                yesterday, today, 10
        );

        long rank = 1;
        for (StringTermsBucket bucket : buckets) {
            HotTopic topic = HotTopic.builder()
                    .topicRank(rank++)
                    .keyword(bucket.key().stringValue())
                    .keywordCount(bucket.docCount())
                    .topicDate(yesterday.atStartOfDay())
                    .build();

            hotTopicRepository.save(topic);
        }
    }

    public List<NewsEsDocument> getNewsList(String keyword, int size) throws IOException {
        return elasticSearchService.searchByKeyword(keyword, size);
    }
}
