package Baemin.News_Deliver.Global.News.ElasticSearch.service;

import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import Baemin.News_Deliver.Global.News.Batch.dto.NewsItemDTO;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * 뉴스 Elasticsearch 색인 서비스
 *
 * <p>전날 수집된 뉴스 데이터를 데이터베이스에서 조회하여,
 * 섹션별로 분리된 리스트를 생성한 후 Elasticsearch에 bulk 색인합니다.</p>
 *
 * <p>색인은 아래와 같은 과정으로 진행됩니다:</p>
 * <ol>
 *     <li>전날 뉴스 중 특정 섹션(section)에 해당하는 뉴스 DB에서 조회</li>
 *     <li>NewsItemDTO → NewsEsDocument로 변환</li>
 *     <li>Elasticsearch의 news-index-nori에 Bulk 색인</li>
 *     <li>에러 발생 시 로그 출력</li>
 * </ol>
 *
 * <p>주의 사항:</p>
 * <ul>
 *     <li>색인 대상 날짜는 항상 전날(CURDATE - 1)</li>
 *     <li>sections는 단일 값으로 저장되며, ES에는 List<String> 형태로 전달됨</li>
 *     <li>Elasticsearch 클러스터 정상 연결 필요</li>
 * </ul>
 *
 * @author 김원중
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsEsService {

    private final JdbcTemplate jdbcTemplate;
    private final ElasticsearchClient client;
    private static final String[] SECTIONS = {
            "politics", "economy", "society", "culture", "tech", "entertainment", "opinion"
    };

    /**
     * 섹션별 뉴스 데이터를 ES에 색인하는 메인 실행 메서드
     */
    public void esBulkService() {
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
     * DB에서 섹션별 뉴스 데이터를 조회
     *
     * @param section 뉴스 섹션
     * @return 뉴스 DTO 리스트
     */
    private List<NewsItemDTO> loadNewsFromDB(String section) {
        String sql = """
            SELECT * FROM news
                WHERE DATE(published_at) = CURDATE() - INTERVAL 1 DAY
                AND sections = ?
                """;

        return jdbcTemplate.query(sql, new Object[]{section}, (rs, rowNum) -> new NewsItemDTO(
                String.valueOf(rs.getLong("id")), // ES에서는 문자열 ID가 안전
                List.of(rs.getString("sections")), // 단일 값이지만 ES는 List<String> 타입
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
     * @param dtoList 뉴스 DTO 리스트
     * @return ES 도큐먼트 리스트
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
     * Elasticsearch에 Bulk 색인 요청 수행
     *
     * @param docs 색인할 뉴스 도큐먼트 목록
     * @param section 섹션명 (로그 출력용)
     * @return 색인 성공 여부 (true: 전체 성공, false: 일부 실패 또는 전체 실패)
     */
    private boolean bulkInsert(List<NewsEsDocument> docs, String section) {
        try {
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

            BulkResponse result = client.bulk(br.build());

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

        } catch (IOException e) {
            log.error("❌ [{}] 섹션 Elasticsearch Bulk 요청 실패: {}", section, e.getMessage(), e);
            return false;
        }
    }

}
