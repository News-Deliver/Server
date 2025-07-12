package Baemin.News_Deliver.Global.NewsMonitoring.Helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Slf4j
public class NewsMonitoringHelper{

    /**
     * Header 정의 메서드
     *
     * @param apiKey Deepsearch api 키
     * @return Http 엔티티
     */
    public static HttpEntity<String> getAuthorizedEntity(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }

    /**
     * Url을 빌드하는 메서드
     *
     * @param section 검색 영역
     * @param pageSize 페이지의 사이즈
     * @param page 페이지의 수
     * @param dateFrom 시작 날짜
     * @param dateTo 마감 날짜
     * @return 최종 빌드 된 Url
     */
    public static String buildUrl(String section, int pageSize, int page, String dateFrom, String dateTo) {
        return String.format(
                "https://api-v2.deepsearch.com/v1/articles/%s?page_size=%d&page=%d&date_from=%s&date_to=%s&order=published_at",
                section, pageSize, page, dateFrom, dateTo);
    }
}
