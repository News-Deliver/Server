package Baemin.News_Deliver.Domain.HotTopic.service;

import Baemin.News_Deliver.Domain.HotTopic.dto.HotTopicResponseDTO;
import Baemin.News_Deliver.Domain.HotTopic.entity.HotTopic;
import Baemin.News_Deliver.Domain.HotTopic.repository.HotTopicRepository;
import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import Baemin.News_Deliver.Global.News.ElasticSearch.service.NewsEsService;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotTopicService {

    private final ElasticsearchClient elasticsearchClient;
    private final HotTopicRepository hotTopicRepository;
    private final NewsEsService elasticSearchService;

    public List<HotTopicResponseDTO> getHotTopicList() {
        return hotTopicRepository.findAll().stream()
                .map(entity -> HotTopicResponseDTO.builder()
                        .topicRank(entity.getTopicRank())
                        .keyword(entity.getKeyword())
                        .keywordCount(entity.getKeywordCount())
                        .topicDate(entity.getTopicDate())
                        .build())
                .toList();
    }

    @Transactional
    public void getAndSaveHotTopic() throws IOException {
        LocalDate now = LocalDate.now();
        LocalDate yesterday = now.minusDays(1);

        List<StringTermsBucket> buckets = elasticSearchService.getTopKeywordsForDateRange(
                yesterday, now, 10
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

        log.info("🔥 HotTopic 저장 완료: {}건", buckets.size());
    }

    public List<NewsEsDocument> getNewsList(String keyword, int size) throws IOException {
        return elasticSearchService.searchByKeyword(keyword, size);
    }
}
