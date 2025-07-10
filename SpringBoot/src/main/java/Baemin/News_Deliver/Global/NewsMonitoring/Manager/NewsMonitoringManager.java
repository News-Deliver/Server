package Baemin.News_Deliver.Global.NewsMonitoring.Manager;

import Baemin.News_Deliver.Global.News.JPAINSERT.dto.NewsItemDTO;
import Baemin.News_Deliver.Global.NewsMonitoring.DTO.NewsSimpleResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsMonitoringManager {

    @Value("${deepsearch.api.key}")
    private String apiKey;
    private static final String API_URL = "https://api-v2.deepsearch.com/v1/articles";

    /**
     * page_size=1로 호출해서 해당 섹션의 total_items를 모니터링
     *
     * 정상 작동 티스트 완료 -> 머지 후 실패로 바뀜
     *
     * @param section 검색 영역
     * @param dateFrom 시작 날짜
     * @param dateTo 마감 날짜
     * @return 해당 섹션의 total item 수
     */
    public int getTotalItems(String section, String dateFrom, String dateTo) {
        RestTemplate restTemplate = new RestTemplate();

        /* 해당 섹션의 total item 수 파악을 위한 http url 생성 */
        String url = String.format("%s/%s?page_size=1&date_from=%s&date_to=%s&order=published_at",
                API_URL, section, dateFrom, dateTo);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        /* http 요청 후 totalItem 최종 반환 */
        try {
            // Http 요청
            ResponseEntity<NewsSimpleResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    NewsSimpleResponseDTO.class // 응답
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                int totalItems = response.getBody().getTotal_items();
                log.info("[{}] total_items 수 : {}", section, totalItems);
                return totalItems;
            } else {
                log.warn("[{}] 응답 실패 or 데이터 없음", section);  /* 예외 커스터마이징 */
            }
        } catch (Exception e) {
            log.error("[{}] total_items 조회 실패: {}", section, e.getMessage()); /* 예외 커스터마이징 */
        }

        /* 논리 오류 : 실패 시 0으로 fallback */
        log.info("실패 : 0으로 fallback");
        return 0;
    }

    /**
     * DeepSearch API를 통한 데이터 중간 수집 메서드
     *
     * 테스트 필요
     *
     * @param section 뉴스 영역
     * @param dateFrom 시작일
     * @param dateTo 마감일
     * @param totalPages 수집할 데이터의 페이지 (100이하)
     * @return 수집된 모든 뉴스 데이터 List로 반환
     */
    public List<NewsItemDTO> fetchAllPages(String section, String dateFrom, String dateTo, int totalPages) {
        List<NewsItemDTO> allNews = new ArrayList<>();

        /* http 요청 메타 데이터 - 다른 코드에도 중복 : 이후 global 처리 하기 */
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        /* 집계된 수만큼만 뉴스 데이터 수집 */
        if(totalPages > 100) totalPages = 100; // 방어 코드 : 토탈 페이지가 100이 넘으면 100으로 Fix
        for (int page = 1; page <= totalPages; page++) {
            String url = String.format("%s/%s?page_size=%d&page=%d&date_from=%s&date_to=%s&order=published_at",
                    API_URL, section, 100, page, dateFrom, dateTo);
            try {
                ResponseEntity<NewsSimpleResponseDTO> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        NewsSimpleResponseDTO.class
                );
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    List<NewsItemDTO> dataList = response.getBody().getData();
                    allNews.addAll(dataList);
                    log.info("[{}] {}페이지 수집 성공, {}건", section, page, dataList.size());
                } else {
                    log.warn("[{}] {}페이지 응답 실패", section, page); /* 예외 커스터 마이징 */
                }
            } catch (Exception e) {
                log.error("[{}] {}페이지 호출 실패: {}", section, page, e.getMessage()); /* 예외 커스터 마이징 */
            }
        }
        return allNews;
    }


}
