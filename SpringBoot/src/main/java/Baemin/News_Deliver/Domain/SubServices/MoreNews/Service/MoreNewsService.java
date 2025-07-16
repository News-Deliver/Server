package Baemin.News_Deliver.Domain.SubServices.MoreNews.Service;

import Baemin.News_Deliver.Domain.Kakao.entity.History;
import Baemin.News_Deliver.Domain.Kakao.repository.HistoryRepository;
import Baemin.News_Deliver.Domain.SubServices.Exception.SubServicesException;
import Baemin.News_Deliver.Domain.SubServices.MoreNews.DTO.GroupedNewsHistoryResponse;
import Baemin.News_Deliver.Domain.SubServices.MoreNews.DTO.NewsHistoryResponse;
import Baemin.News_Deliver.Global.Exception.ErrorCode;
import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoreNewsService {

    private final HistoryRepository historyRepository;
    private final ElasticsearchClient client;

    // ======================= 뉴스 추가 검색 메서드 =========================

    /**
     * 뉴스 추가 검색 메서드
     *
     * @param historyId 히스토리 고유 번호
     * @return 15개의 뉴스 기사 리스트
     * @throws IOException IO 예외
     */
    public List<NewsEsDocument> getMoreNews(Long historyId) throws IOException {

        /* 히스토리 객체 반환 */
        History history = historyRepository.findById(historyId)
                .orElseThrow(() -> new SubServicesException(ErrorCode.HISTORY_NOT_FOUND));
        log.info("히스토리 객체 반환 성공 : {}", history);

        /* 히스토리 세부 정보 반환 */
        String settingKeyword = history.getSettingKeyword(); // 설정 키워드 반환
        String blockKeyword = history.getBlockKeyword(); // 제외 키워드
        LocalDateTime publishedAt = history.getPublishedAt(); // 날짜
        log.info("히스토리 세부 정보 반환 (settingKeyword,publishedAt, blockKeyword) : {},{},{}", settingKeyword,publishedAt, blockKeyword);

        // settingKeyword 문자열을 리스트로 변환
        List<String> settingKeywords = Arrays.stream(settingKeyword.split(","))
                .map(String::trim)        // 공백 제거
                .filter(s -> !s.isEmpty()) // 빈 문자열 제거
                .toList();
        log.info("설정 리스트 반환 : {}", settingKeywords);

        // blockKeyword 문자열을 리스트로 변환
        List<String> blockKeywords = Arrays.stream(blockKeyword.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        log.info("제외 리스트 반환 : {}", blockKeywords);

        /* Elastic Search 검색 엔진 호출 : List 반환 */

        // 포함 키워드 필터링
        // 포함 키워드 쿼리
        Query includeKeywordQuery = Query.of(q -> q
                .bool(b -> b
                        .should(settingKeywords.stream()
                                .map(kw -> Query.of(q2 -> q2
                                        .multiMatch(m -> m
                                                .query(kw)
                                                .fields("title^3", "summary^2")
                                                .type(TextQueryType.BoolPrefix)
                                        )
                                ))
                                .collect(Collectors.toList())
                        )
                        .minimumShouldMatch("1")
                )
        );

        // 제외 키워드 쿼리
        Query excludeKeywordQuery = Query.of(q -> q
                .bool(b -> b
                        .should(blockKeywords.stream()
                                .map(kw -> Query.of(q2 -> q2
                                        .multiMatch(m -> m
                                                .query(kw)
                                                .fields("title^3", "summary^2")
                                                .type(TextQueryType.BoolPrefix)
                                        )
                                ))
                                .collect(Collectors.toList())
                        )
                )
        );

        // 날짜 필터링
        Query dateFilter = Query.of(q -> q
                .range(r -> r
                        .field("published_at") // 메시지 전송일
                        //.gte(JsonData.of(publishedAt))
                        // 개선:
                        .gte(JsonData.of(publishedAt.minusDays(1)))
                )
        );

        // 전체 쿼리 (키워드와 블랙 키워드를 포함한 전체 쿼리)
        Query finalQuery = Query.of(q -> q
                .bool(b -> b
                        .must(includeKeywordQuery)
                        .must(dateFilter)
                        .mustNot(excludeKeywordQuery)
                )
        );

        // 검색 엔진
        SearchRequest request = SearchRequest.of(s -> s
                .index("news-index-nori")
                .query(finalQuery)
                .size(15)
                .sort(sort -> sort
                        .score(sc -> sc.order(co.elastic.clients.elasticsearch._types.SortOrder.Desc))
                )
        );
        log.info("검색 엔진 결과  : {}", request);

        SearchResponse<NewsEsDocument> response = client.search(request, NewsEsDocument.class);
        log.info("SearchResponse<NewsEsDocument> response : {}", response);

        response.hits().hits().forEach(hit -> {
            assert hit.source() != null;
            log.info(" {} | score: {}", hit.source().getTitle(), hit.score());
        });

        // 그대로 ES 문서 리스트 반환
        return response.hits().hits().stream()
                .map(Hit::source)
                .toList();
    }

    // ======================= 내 히스토리 조회하기 메서드 =========================


    /**
     * 내 히스토리 조회하기 메서드 (페이지 네이션 적용)
     *
     * @param page 시작 페이지
     * @param size 페이지 사이즈
     * @return 페이지 네이션이 적용된 히스토리
     */
    public List<GroupedNewsHistoryResponse> getGroupedNewsHistory(int page, int size) {
        Long userId = 1L;

        List<History> allHistories = historyRepository.findAllBySetting_User_Id(userId);

        /* 1. 그룹핑: settingId + publishedAt (시 단위로 자르기) */
        Map<String, List<History>> grouped = allHistories.stream()
                .collect(Collectors.groupingBy(h -> {
                    Long settingId = h.getSetting().getId();
                    LocalDateTime truncatedPublishedAt = h.getPublishedAt().truncatedTo(ChronoUnit.HOURS);
                    return settingId + "_" + truncatedPublishedAt;
                }));

        /* 2. GroupedNewsHistoryResponse 리스트로 변환 */
        List<GroupedNewsHistoryResponse> groupedList = grouped.entrySet().stream()
                .map(entry -> {
                    List<History> histories = entry.getValue();
                    History any = histories.get(0); // 그룹 내 대표 뉴스

                    return GroupedNewsHistoryResponse.builder()
                            .settingId(any.getSetting().getId())
                            .publishedAt(any.getPublishedAt().truncatedTo(ChronoUnit.HOURS)) // 대표 시간
                            .settingKeyword(any.getSettingKeyword()) // 대표 키워드
                            .blockKeyword(any.getBlockKeyword())     // 대표 제외 키워드
                            .newsList(histories.stream()
                                    .map(NewsHistoryResponse::from)
                                    .collect(Collectors.toList()))
                            .build();
                })

                .sorted(Comparator.comparing(GroupedNewsHistoryResponse::getPublishedAt).reversed()) // 최신순
                .collect(Collectors.toList());

        /* 3. 페이지네이션 */
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, groupedList.size());

        if (fromIndex >= groupedList.size()) {
            return Collections.emptyList();
        }

        return groupedList.subList(fromIndex, toIndex);
    }



}
