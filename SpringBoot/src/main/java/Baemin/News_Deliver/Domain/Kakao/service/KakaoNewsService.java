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

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoNewsService {

    private final ElasticsearchClient client;

    public List<NewsEsDocument> searchNews(String keyword, String blockKeyword) {

        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            //키워드 추출
            Query includeKeyword = Query.of(q -> q
                    .multiMatch(m -> m
                            .query(keyword)
                            .fields("title", "summary", "content_url", "publisher")
                            .type(TextQueryType.BoolPrefix)
                    )
            );

            // 블랙 키워드 쿼리
            Query excludeKeyword = Query.of(q -> q
                    .bool(b -> b
                            .should(s -> s
                                    .multiMatch(m -> m
                                            .query(blockKeyword)
                                            .fields("title", "summary", "content_url", "publisher")
                                            .type(TextQueryType.BoolPrefix)
                                    )
                            )
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


            // 전체 쿼리 (키워드와 블랙 키워드를 포함한 전체 쿼리)
            Query finalQuery = Query.of(q -> q
                    .bool(b -> b
                            .must(includeKeyword)
                            .must(dateFilter)
                            .mustNot(excludeKeyword)
                    )
            );



            // 검색 엔진
            SearchRequest request = SearchRequest.of(s -> s
                    .index("news-index-nori")
                    .query(finalQuery)
                    .size(5)
                    .sort(sort -> sort
                            .score(sc -> sc.order(co.elastic.clients.elasticsearch._types.SortOrder.Desc))
                    )
            );

            SearchResponse<NewsEsDocument> response = client.search(request, NewsEsDocument.class);

            // 스코어 확인 코드
            response.hits().hits().forEach(hit ->
                    log.info("✅ {} | score: {}", hit.source().getTitle(), hit.score())
            );

            // 반환
            return response.hits().hits().stream()
                    .map(hit -> {
                        NewsEsDocument doc = hit.source();
                        return doc;
                    })
                    .toList();

        } catch (IOException e) {
            log.error("키워드 기반 뉴스 검색 실패: {}", e.getMessage(), e);
            return List.of();
        }
    }
}