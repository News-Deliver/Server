package Baemin.News_Deliver.Global.News.ElasticSearch.service;

import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import Baemin.News_Deliver.Global.News.Batch.dto.NewsItemDTO;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 뉴스 Elasticsearch 색인 및 검색 서비스
 *
 * <p>해당 서비스는 다음의 역할을 수행합니다:</p>
 *
 * <ul>
 *     <li>전날 수집된 뉴스 데이터를 DB에서 조회 후, Elasticsearch에 Bulk 색인</li>
 *     <li>Elasticsearch에서 키워드 기반 뉴스 검색</li>
 *     <li>지정 날짜 범위 내에서 인기 키워드(terms aggregation) 추출</li>
 * </ul>
 *
 * 색인 대상 인덱스: {@code news-index-nori}
 * 검색 필드: {@code combinedTokens} (제목 + 요약 통합 필드)
 * 색인 기준 날짜: {@code CURDATE() - INTERVAL 1 DAY}
 *
 * @author 김원중
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsEsService {

    private final JdbcTemplate jdbcTemplate;
    private final ElasticsearchClient elasticsearchClient;
    private static final String[] SECTIONS = {
            "politics", "economy", "society", "culture", "tech", "entertainment", "opinion"
    };

    /**
     * 섹션별 뉴스 데이터를 Elasticsearch에 Bulk 색인하는 메서드
     *
     * <p>전날 수집된 뉴스 중 각 섹션(section)에 해당하는 데이터를 DB에서 조회하고,
     * {@link NewsEsDocument} 형식으로 변환한 후 ES에 색인합니다.</p>
     *
     * 색인 대상 인덱스: {@code news-index-nori}
     */
    public void esBulkService() throws IOException {
        long totalStart = System.nanoTime(); // 전체 시작 시간
        int totalCount = 0;
        int totalFailureCount = 0;

        for (String section : SECTIONS) {
            long sectionStart = System.nanoTime(); // 섹션 시작 시간
            log.info("📌 [{}] 섹션 색인 시작", section);

            List<NewsItemDTO> dtoList = loadNewsFromDB(section);
            if (dtoList.isEmpty()) {
                log.info("ℹ️ [{}] 섹션에 색인할 데이터가 없습니다.", section);
                continue;
            }

            List<NewsEsDocument> docs = convertToEsDocuments(dtoList);
            boolean success = bulkInsert(docs, section);

            long sectionTime = System.nanoTime() - sectionStart;
            log.info("⏱️ [{}] 섹션 처리 시간: {}ms", section, sectionTime / 1_000_000);

            totalCount += docs.size();
            if (!success) totalFailureCount += docs.size();
        }

        long totalTime = System.nanoTime() - totalStart;
        log.info("✅ 전체 색인 완료: 총 {}건 처리 (실패: {}건)", totalCount, totalFailureCount);
        log.info("⏱️ 전체 소요 시간: {}ms", totalTime / 1_000_000);
    }

    /**
     * 전날 뉴스 중 특정 섹션에 해당하는 데이터를 DB에서 조회
     *
     * @param section 뉴스 섹션 (예: politics, economy)
     * @return 해당 섹션의 뉴스 DTO 리스트
     */
    private List<NewsItemDTO> loadNewsFromDB(String section) {
        String sql = """
            SELECT *
            FROM news
            WHERE published_at >= CURDATE() - INTERVAL 1 DAY
              AND published_at < CURDATE()
              AND sections = ?
                """;

        return jdbcTemplate.query(sql, new Object[]{section}, (rs, rowNum) -> new NewsItemDTO(
                String.valueOf(rs.getLong("id")),
                List.of(rs.getString("sections")),
                rs.getString("title"),
                rs.getString("publisher"),
                rs.getString("summary"),
                rs.getString("content_url"),
                rs.getTimestamp("published_at").toLocalDateTime()
        ));
    }

    /**
     * DTO → Elasticsearch 도큐먼트 변환
     *
     * @param dtoList DB에서 불러온 뉴스 DTO 리스트
     * @return 변환된 Elasticsearch 도큐먼트 리스트
     */
    private List<NewsEsDocument> convertToEsDocuments(List<NewsItemDTO> dtoList) {
        return dtoList.stream()
                .map(dto -> NewsEsDocument.builder()
                        .id(dto.getId())
                        .sections(dto.getSections().get(0))
                        .title(dto.getTitle())
                        .publisher(dto.getPublisher())
                        .summary(dto.getSummary())
                        .content_url(dto.getContent_url())
                        .published_at(dto.getPublished_at())
                        .build())
                .toList();
    }

    /**
     * Elasticsearch에 Bulk 색인 요청을 수행
     *
     * @param docs 색인할 뉴스 도큐먼트 리스트
     * @param section 섹션명 (로그 용도)
     * @return 전체 색인 성공 여부 (true: 성공, false: 일부 실패 발생)
     * @throws IOException Elasticsearch 통신 실패 시 발생
     */
    private boolean bulkInsert(List<NewsEsDocument> docs, String section) throws IOException {
        BulkRequest.Builder br = new BulkRequest.Builder();

        for (NewsEsDocument doc : docs) {
            br.operations(op -> op
                    .index(idx -> idx
                            .index("news-index-nori")
                            .id(doc.getId())
                            .document(doc)
                    )
            );
        }

        BulkResponse result = elasticsearchClient.bulk(br.build());

        if (result.errors()) {
            log.warn("⚠️ [{}] 색인 중 일부 실패 발생", section);
            result.items().stream()
                    .filter(item -> item.error() != null)
                    .forEach(item ->
                            log.warn("❌ 에러 - ID: {}, 이유: {}", item.id(), item.error().reason()));
            return false;
        } else {
            log.info("✅ [{}] 섹션 색인 성공: {}건", section, docs.size());
            return true;
        }
    }

    /**
     * 키워드로 뉴스 기사 검색
     *
     * <p>{@code combinedTokens} 필드에 대해 match 쿼리를 수행하고,
     * 점수(score) 기준으로 내림차순 정렬된 상위 결과를 반환합니다.</p>
     *
     * @param keyword 검색 키워드
     * @param size 최대 검색 결과 수
     * @return 검색된 뉴스 도큐먼트 리스트
     * @throws IOException Elasticsearch 검색 실패 시 발생
     */
    public List<NewsEsDocument> searchByKeyword(String keyword, int size) throws IOException {
        SearchResponse<NewsEsDocument> response = elasticsearchClient.search(s -> s
                        .index("news-index-nori")
                        .size(size)
                        .query(q -> q
                                .match(m -> m
                                        .field("combinedTokens")
                                        .query(keyword)
                                )
                        )
                        .sort(sort -> sort
                                .score(sc -> sc.order(SortOrder.Desc))
                        ),
                NewsEsDocument.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 날짜 범위 내 인기 키워드(terms aggregation) 추출
     *
     * <p>{@code combinedTokens} 필드에 대해 terms aggregation을 수행하여
     * 지정한 날짜 범위 내에서 가장 많이 등장한 키워드 Top N을 추출합니다.</p>
     *
     * @param gte 시작 날짜 (포함)
     * @param lt 종료 날짜 (미포함)
     * @param size 상위 키워드 개수 (예: 10)
     * @return 키워드 집계 결과 리스트 (StringTermsBucket)
     * @throws IOException Elasticsearch 요청 실패 시 발생
     */
    public List<StringTermsBucket> getTopKeywordsForDateRange(LocalDate gte, LocalDate lt, int size) throws IOException {
        SearchResponse<Void> response = elasticsearchClient.search(s -> s
                .index("news-index-nori")
                .size(0)
                .query(q -> q.range(r -> r
                        .field("published_at")
                        .gte(JsonData.of(gte.toString()))
                        .lt(JsonData.of(lt.toString()))
                ))
                .aggregations("top_combined_keywords", a -> a
                        .terms(t -> t
                                .field("combinedTokens")
                                .size(size)
                        )
                ), Void.class);

        return response.aggregations()
                .get("top_combined_keywords")
                .sterms()
                .buckets()
                .array();
    }
}
