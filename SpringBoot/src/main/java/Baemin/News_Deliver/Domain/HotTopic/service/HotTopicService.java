package Baemin.News_Deliver.Domain.HotTopic.service;

import Baemin.News_Deliver.Domain.HotTopic.entity.HotTopic;
import Baemin.News_Deliver.Domain.HotTopic.repository.HotTopicRepository;
import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public void logRelatedArticlesForHotTopics() throws IOException {
        LocalDate date = LocalDate.now().minusDays(1);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        // 1. DB에서 어제의 핫토픽 10개 가져오기
        List<HotTopic> hotTopics = hotTopicRepository.findTop10ByTopicDateBetweenOrderByTopicRankAsc(start, end);
        log.info("===== [어제 핫토픽 10개] =====");

        for (HotTopic topic : hotTopics) {
            String keyword = topic.getKeyword();
            log.info(">>> 🔥 [{}위] 키워드: {}", topic.getTopicRank(), keyword);

            // 2. 키워드로 연관 기사 20개 조회
            SearchResponse<NewsEsDocument> response = elasticsearchClient.search(s -> s
                    .index("news-index-nori")
                    .size(20)
                    .query(q -> q
                            .match(m -> m
                                    .field("combinedTokens")
                                    .query(keyword)
                            )
                    ), NewsEsDocument.class);

            List<Hit<NewsEsDocument>> hits = response.hits().hits();

            log.info("관련 기사 수: {}", hits.size());

            for (Hit<NewsEsDocument> hit : hits) {
                NewsEsDocument doc = hit.source();
                if (doc != null) {
                    log.info(" - 기사 ID: {}, 제목: {}", doc.getId(), doc.getTitle());
                }
            }

            log.info("----------");
        }

    }

    @Transactional
    public void getAndSaveHotTopic() {
        try {
            // 어제 날짜 계산
            LocalDate now = LocalDate.now();
            LocalDate yesterday = now.minusDays(1);

            String gte = yesterday.toString();
            String lt = now.toString();

            // Elasticsearch에서 어제 날짜 기준 키워드 집계
            SearchResponse<Void> response = elasticsearchClient.search(s -> s
                            .index("news-index-nori")
                            .size(0)
                            .query(q -> q.range(r -> r
                                    .field("published_at")
                                    .gte(JsonData.of(gte))
                                    .lt(JsonData.of(lt))
                            ))
                            .aggregations("top_combined_keywords", a -> a
                                    .terms(t -> t
                                            .field("combinedTokens")
                                            .size(10)
                                    )
                            )
                    , Void.class);

            List<StringTermsBucket> combinedBuckets = response.aggregations()
                    .get("top_combined_keywords")
                    .sterms()
                    .buckets().array();

            // 순위 저장
            long rank = 1;
            for (StringTermsBucket bucket : combinedBuckets) {
                HotTopic topic = HotTopic.builder()
                        .topicRank(rank++)
                        .keyword(bucket.key().stringValue())
                        .topicDate(yesterday.atStartOfDay())
                        .build();

                hotTopicRepository.save(topic);
            }

            log.info("🔥 HotTopic 저장 완료: {}건", combinedBuckets.size());

        } catch (Exception e) {
            log.error("🔥 Elasticsearch 요청 실패: {}", e.getMessage());
        }
    }
}
