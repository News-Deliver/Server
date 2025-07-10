package Baemin.News_Deliver.Global.NewsMonitoring.Service;


import Baemin.News_Deliver.Global.News.JPAINSERT.dto.NewsItemDTO;
import Baemin.News_Deliver.Global.NewsMonitoring.DTO.NewsSimpleResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsMonitoringService {

    @Value("${deepsearch.api.key}")
    private String apiKey;
    private static final String API_URL = "https://api-v2.deepsearch.com/v1/articles";
    private static final String[] sections = {"politics", "economy", "society", "culture", "tech", "entertainment", "opinion"};

    /**
     * 매 정각마다 실행 되는 모니터링 스케줄러
     *
     * 각 섹션 별 현재 몇개의 데이터가 모였는지 모니터링
     * 9000개가 넘는 섹션 발견 시, 즉시 DB에 Batch 실시
     *
     */
    @Scheduled(cron = "0 0 * * * *")
    public void monitoring(){

        LocalDate day = LocalDate.now(); // 오늘의 날짜
        log.info("오늘의 날짜 : {}", day); // 오늘의 날짜 로그 확인
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); //원하는 포맷 지정

        String dateTo = day.format(formatter); //문자열로 변환
        String dateFrom = day.format(formatter); //문자열로 변환

        int pageSize = 100; // 요청할 페이지의 사이즈

        for (String section : sections) {

            // page_size=1로 호출해서 해당 섹션의 total_items를 모니터링
            int total_items = getTotalItems(dateFrom, dateTo, section);

            /* Redis에서 NewsBatchCount-{섹션 명} : n 에서 n 추출 */
            // 일단은 n=0으로 가정 -> redis 온전히 구현 안되어 있음
            int n = 0 ;

            /**
             * total_items >= 9000*(n+1) 계산
             * T일 시 : 바로 데이터 9000개 수집 후 Mysql에 Batch 처리
             * F일 시 : pass
             */
            if(total_items >= 9000*(n+1)){

                /* 1. 바로 DeepSearch API 호출 */
                log.info("[{}] 뉴스 데이터 9000개 이상으로 집계 - DeepSearch API 호출 시도",section);

                // 호출해야 할 페이지 수 계산
                int total_pages = total_items/100;
                log.info("페이지 수 : {}", total_pages);

                // 뉴스 기사 데이터 리스트 추출
                List<NewsItemDTO> newsItemDTOList = fetchAllPages(section,dateFrom,dateTo,total_pages);
                log.info("[{}] 뉴스 데이터 9000개 이상으로 집계 - DeepSearch API 호출 완료",section);

                /* 2. Batch 처리 진행 */
                log.info("[{}] 뉴스 데이터 9000개 이상으로 집계 - Batch 처리 시작",section);

                // Batch 메서드 선언 //

                log.info("[{}] 뉴스 데이터 9000개 이상으로 집계 - Batch 처리 완료",section);

                /* 3. 해당 섹션은 Redis에 저장 */
                // 추후 자정 Batch 작업 때, 수집한 만큼 Minus 한 후 수집 진행
                log.info("[{}] 해당 섹션 정보 Redis에 저장 : 추후 자정 Batch시 반영",section);

            }else{
                log.info("[{}] DB에 Batch 처리 생략",section);
            }

        }
    }

    /**
     * page_size=1로 호출해서 해당 섹션의 total_items를 모니터링
     *
     * 정상 작동 티스트 완료
     *
     * @param section 검색 영역
     * @param dateFrom 시작 날짜
     * @param dateTo 마감 날짜
     * @return 해당 섹션의 total item 수
     */
    private int getTotalItems(String section, String dateFrom, String dateTo) {
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
                log.info("[{}] total_items: {}", section, totalItems);
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
    private List<NewsItemDTO> fetchAllPages(String section, String dateFrom, String dateTo, int totalPages) {
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

    // ======================= Test Method =========================

    /**
     * 섹션 별 뉴스 숫자 집계
     *
     */
    public void testMonitoring(){

        LocalDate day = LocalDate.now(); // 오늘의 날짜
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); //원하는 포맷 지정

        String dateTo = day.format(formatter); //문자열로 변환
        String dateFrom = day.format(formatter); //문자열로 변환

        int pageSize = 100; // 요청할 페이지의 사이즈

        log.info("오늘의 날짜 : {}", day); // 오늘의 날짜 로그 확인
        // long totalStart = System.nanoTime(); // 전체 시작 시간

        for (String section : sections) {

            // page_size=1로 호출해서 해당 섹션의 total_items를 모니터링
            int total_items = getTotalItems( section, dateFrom, dateTo);
            log.info("[{}] 수집된 total_items: {}", section, total_items);
        }
    }
}
