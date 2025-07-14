package Baemin.News_Deliver.Domain.SubServices.MoreNews.Service;

import Baemin.News_Deliver.Domain.Kakao.entity.History;
import Baemin.News_Deliver.Domain.Kakao.entity.SettingBlockKeywordHistory;
import Baemin.News_Deliver.Domain.Kakao.entity.SettingKeywordHistory;
import Baemin.News_Deliver.Domain.Kakao.repository.HistoryRepository;
import Baemin.News_Deliver.Domain.Kakao.repository.SettingBlockKeywordHistoryRepository;
import Baemin.News_Deliver.Domain.Kakao.repository.SettingKeywordHistoryRepository;
import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import Baemin.News_Deliver.Domain.SubServices.Exception.SubServicesException;
import Baemin.News_Deliver.Domain.SubServices.MoreNews.DTO.MoreNewsDTO;
import Baemin.News_Deliver.Global.Exception.ErrorCode;
import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoreNewsService {

    private final ElasticsearchClient elasticsearchClient;
    private final HistoryRepository historyRepository;
    private  final SettingKeywordHistoryRepository settingKeywordHistoryRepository;
    private final SettingBlockKeywordHistoryRepository settingBlockKeywordHistoryRepository;

    /**
     * 뉴스 더보기 메서드
     *
     * @param historyId 더보기를 누를 History의 고유 번호 
     * @return 추가적으로 반환 받은 뉴스 리스트(20개)
     * @throws IOException IO 예외
     */
    @Transactional(readOnly = true)
    public List<MoreNewsDTO> searchMoreNews(Long historyId) throws IOException {

        /* 히스토리 조회 */
        History history = historyRepository.findById(historyId)
                .orElseThrow(() -> new SubServicesException(ErrorCode.HISTORY_NOT_FOUND));
        Setting setting = history.getSetting(); // 설정 키
        LocalDateTime publishedAt = history.getPublishedAt(); // 발행 날짜
        log.info("히스토리 조회 성공");

        /* 히스토리에서 키워드 조회 */
        List<String> keywords = settingKeywordHistoryRepository.findByHistory(history)
                .stream()
                .map(SettingKeywordHistory::getSettingKeyword)
                .toList();
        log.info("히스토리에서 키워드 조회 성공 조회 성공");

        // 키워드 유효성 검사 : 제외 키워드와 달리, 키워드는 무조건 있어야 한다.
        if (keywords.isEmpty()) {
            log.warn("키워드 없음: settingId = {}", setting.getId());
            return Collections.emptyList();
        }
        log.info("히스토리 유효성 검사 성공");

        /* 히스토리에서 제외 키워드 조회 */
        List<String> blockKeywords = settingBlockKeywordHistoryRepository.findByHistory(history)
                .stream()
                .map(SettingBlockKeywordHistory::getBlockKeyword)
                .toList();
        log.info("히스토리 제외 키워드 조회 성공");

        /* Elasticsearch 쿼리 구성 */
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // 날짜 필터링
        boolQuery.filter(q -> q.range(r -> r
                .field("published_at")
                .gte(JsonData.of(publishedAt)) // LocalDateTime을 JsonData로 변환
        ));
        log.info("날짜 필터링 성공");

        // 포함 키워드 (match should) : 이 중 하나만이라도 포함
        boolQuery.must(m -> m.bool(b -> b.should(
                keywords.stream()
                        .map(k -> Query.of(q -> q.match(MatchQuery.of(mq -> mq
                                .field("combinedTokens")
                                .query(k)
                        )))).toList()
        )));
        log.info("키워드 포함 성공");

        // 제외 키워드 (must_not) : 이 중 하나라도 있으면 안됌
        for (String block : blockKeywords) {
            boolQuery.mustNot(q -> q.match(m -> m
                    .field("combinedTokens")
                    .query(block)
            ));
        }
        log.info("키워드 제외 성공");

        /* 검색 실행 */
        SearchResponse<NewsEsDocument> response = elasticsearchClient.search(s -> s
                        .index("news-index-nori")  // 검색할 인덱스명
                        .query(q -> q.bool(boolQuery.build())) // bool 쿼리 조건 설정
                        .size(25),
                NewsEsDocument.class);
        log.info("검색 성공");

        /* 결과 파싱 */
        List<MoreNewsDTO> result = response.hits().hits().stream()
                .map(Hit::source) // 각 히트(hit)에서 실제 문서 데이터(NewsEsDocument)만 추출
                .filter(Objects::nonNull) // null 필터링
                .map(doc -> MoreNewsDTO.builder()
                        .title(doc.getTitle())
                        .summary(doc.getSummary())
                        .contentUrl(doc.getContent_url())
                        .publishedAt(doc.getPublished_at())
                        .build()) // NewsEsDocument → MoreNewsDTO 변환
                .collect(Collectors.toList()); // 리스트로 수집
        log.info("검색 결과 파싱 성공");

        log.info("뉴스 더보기 결과 {}건 반환 (historyId: {})", result.size(), historyId);

        return result;
    }
}
