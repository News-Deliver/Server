package Baemin.News_Deliver.Domain.Kakao.service;

import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoNewsService {

    private final ElasticsearchClient client;

    /**
     * Hot Fixed
     *
     *
     * @param includeKeywords 포함 키워드
     * @param blockKeywords 제외 키워드
     * @return 뉴스 리스트
     */
    public List<NewsEsDocument> searchNews(List<String> includeKeywords, List<String> blockKeywords) {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);

            Query includeKeywordQuery = Query.of(q -> q
                    .bool(b -> b
                            .should(includeKeywords.stream()
                                    .map(kw -> Query.of(q2 -> q2
                                            .multiMatch(m -> m
                                                    .query(kw)
                                                    .fields("title", "summary", "content_url", "publisher")
                                                    .type(TextQueryType.BoolPrefix)
                                            )
                                    ))
                                    .collect(Collectors.toList())
                            )
                            .minimumShouldMatch("1")
                    )
            );

            Query dateFilter = Query.of(q -> q
                    .range(r -> r
                            .field("published_at")
                            .gte(JsonData.of(yesterday.toString()))
                            .lte(JsonData.of(yesterday.toString()))
                            .format("yyyy-MM-dd")
                    )
            );

            // 쿼리 리스트 조합
            List<Query> mustQueries = new ArrayList<>();
            mustQueries.add(includeKeywordQuery);
            mustQueries.add(dateFilter);

            // 제외 키워드가 있는 경우에만 must_not 추가
            Query finalQuery;
            if (blockKeywords != null && !blockKeywords.isEmpty()) {
                Query excludeKeywordQuery = Query.of(q -> q
                        .bool(b -> b
                                .should(blockKeywords.stream()
                                        .map(kw -> Query.of(q2 -> q2
                                                .multiMatch(m -> m
                                                        .query(kw)
                                                        .fields("title", "summary", "content_url", "publisher")
                                                        .type(TextQueryType.BoolPrefix)
                                                )
                                        ))
                                        .collect(Collectors.toList())
                                )
                        )
                );

                finalQuery = Query.of(q -> q
                        .bool(b -> b
                                .must(mustQueries)
                                .mustNot(excludeKeywordQuery)
                        )
                );
            } else {
                finalQuery = Query.of(q -> q
                        .bool(b -> b
                                .must(mustQueries)
                        )
                );
            }

            SearchRequest request = SearchRequest.of(s -> s
                    .index("news-index-nori")
                    .query(finalQuery)
                    .size(5)
                    .sort(sort -> sort
                            .score(sc -> sc.order(co.elastic.clients.elasticsearch._types.SortOrder.Desc))
                    )
            );

            SearchResponse<NewsEsDocument> response = client.search(request, NewsEsDocument.class);

            response.hits().hits().forEach(hit ->
                    log.info("{} | score: {}", hit.source().getTitle(), hit.score())
            );

            return response.hits().hits().stream()
                    .map(hit -> hit.source())
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("키워드 기반 뉴스 검색 실패: {}", e.getMessage(), e);
            return List.of();
        }
    }

    // ======================= Deprecated =========================

//    /**
//     * 사용자의 키워드를 리스트로 묶어 뉴스 검색 메서드에 전달
//     *
//     * @param includeKeywords 포함 키워드
//     * @param blockKeywords 제외 키워드
//     * @return
//     */
//    public List<NewsEsDocument> searchNews(List<String> includeKeywords, List<String> blockKeywords) {
//        try {
//
//            /* 전날 기준으로 시간을 측정 */
//            LocalDate yesterday = LocalDate.now().minusDays(1);
//
//            /* 포함 키워드 쿼리 */
//            Query includeKeywordQuery = Query.of(q -> q
//                    .bool(b -> b
//                            .should(includeKeywords.stream()
//                                    .map(kw -> Query.of(q2 -> q2
//                                            .multiMatch(m -> m
//                                                    .query(kw)
//                                                    .fields("title", "summary", "content_url", "publisher")
//                                                    .type(TextQueryType.BoolPrefix)
//                                            )
//                                    ))
//                                    .collect(Collectors.toList())
//                            )
//                            .minimumShouldMatch("1")
//                    )
//            );
//
//            /* 제외 키워드 쿼리 */
//            Query excludeKeywordQuery = Query.of(q -> q
//                    .bool(b -> b
//                            .should(blockKeywords.stream()
//                                    .map(kw -> Query.of(q2 -> q2
//                                            .multiMatch(m -> m
//                                                    .query(kw)
//                                                    .fields("title", "summary", "content_url", "publisher")
//                                                    .type(TextQueryType.BoolPrefix)
//                                            )
//                                    ))
//                                    .collect(Collectors.toList())
//                            )
//                    )
//            );
//
//            /* 날짜 필터 쿼리 */
//            Query dateFilter = Query.of(q -> q
//                    .range(r -> r
//                            .field("published_at")
//                            .gte(JsonData.of(yesterday.toString()))
//                            .lte(JsonData.of(yesterday.toString()))
//                            .format("yyyy-MM-dd")
//                    )
//            );
//
//            /* 전체 쿼리 조합 */
//            Query finalQuery = Query.of(q -> q
//                    .bool(b -> b
//                            .must(includeKeywordQuery)
//                            .must(dateFilter)
//                            .mustNot(excludeKeywordQuery)
//                    )
//            );
//
//            /* 검색 요청 */
//            SearchRequest request = SearchRequest.of(s -> s
//                    .index("news-index-nori")
//                    .query(finalQuery)
//                    .size(5)
//                    .sort(sort -> sort
//                            .score(sc -> sc.order(co.elastic.clients.elasticsearch._types.SortOrder.Desc))
//                    )
//            );
//
//            /* 검색 수행 */
//            SearchResponse<NewsEsDocument> response = client.search(request, NewsEsDocument.class);
//
//            /* 로그: 스코어 확인 */
//            response.hits().hits().forEach(hit ->
//                    log.info("{} | score: {}", hit.source().getTitle(), hit.score())
//            );
//
//            /* 결과 반환 */
//            return response.hits().hits().stream()
//                    .map(hit -> hit.source())
//                    .collect(Collectors.toList());
//
//        } catch (IOException e) {
//            log.error("키워드 기반 뉴스 검색 실패: {}", e.getMessage(), e);
//            return List.of();
//        }
//    }
}