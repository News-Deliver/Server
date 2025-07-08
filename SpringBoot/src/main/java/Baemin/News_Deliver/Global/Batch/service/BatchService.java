package Baemin.News_Deliver.Global.Batch.service;

import Baemin.News_Deliver.Global.Batch.dto.NewsItemDTO;
import Baemin.News_Deliver.Global.Batch.dto.NewsResponseDTO;
import Baemin.News_Deliver.Global.Batch.entity.News;
import Baemin.News_Deliver.Global.Batch.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final NewsRepository newsRepository;

    private static final String API_URL = "https://api-v2.deepsearch.com/v1/articles";
    private static final String API_KEY = "Bearer a1464d64861841fd9133e01f47f4cff5";
    //FIXME
    //API_KEY 환경 변수로 뺄 것.

    //sections의 값들
    private static final String[] sections = {"politics", "economy", "society", "culture", "tech", "entertainment", "opinion"};

    public void batch() {

        //하루 전날 날짜 구하기
        LocalDate day = LocalDate.now().minusDays(1);

        //원하는 포맷 지정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        //문자열로 변환
        String dateTo = day.format(formatter);
        String dateFrom = day.format(formatter);

        int pageSize = 100;

        System.out.println("검색한 기간 : " + dateFrom + " ~ " + dateTo);

        long totalStart = System.nanoTime(); // 전체 시작 시간

        for (String section : sections) {
            long sectionStart = System.nanoTime(); // 섹션별 시작 시간

            // 먼저 page=1 호출 → total_pages 얻기
            int totalPages = fetchAndSavePage(1, section, pageSize, dateFrom, dateTo);

            for (int page = 2; page <= totalPages; page++) {
                fetchAndSavePage(page, section, pageSize, dateFrom, dateTo);
            }

            long sectionEnd = System.nanoTime(); // 섹션별 종료 시간
            double sectionDuration = (sectionEnd - sectionStart) / 1_000_000_000.0;
            System.out.printf("✅ [%s] 저장 완료 (총 %.2f초)\n", section, sectionDuration);
        }

        long totalEnd = System.nanoTime(); // 전체 종료 시간
        double totalDuration = (totalEnd - totalStart) / 1_000_000_000.0;
        System.out.printf("✅✅ 전체 배치 작업 완료 (총 %.2f초)\n", totalDuration);

    }

    private int fetchAndSavePage(int page, String section, int pageSize, String dateFrom, String dateTo) {
        RestTemplate restTemplate = new RestTemplate();

        // 쿼리 파라미터 구성
        String url = String.format("%s/%s?page_size=%d&date_to=%s&date_from=%s&order=published_at&page=%d",
                API_URL, section, pageSize, dateTo, dateFrom, page);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<NewsResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                NewsResponseDTO.class
        );

        NewsResponseDTO body = response.getBody();
        if (body != null && body.getData() != null) {
            for (NewsItemDTO item : body.getData()) {
                News news = new News();
                news.setTitle(item.getTitle());
                news.setSummary(item.getSummary());
                news.setContentUrl(item.getContent_url());
                news.setPublishedAt(item.getPublished_at());
                news.setSections(section);
                news.setPublisher(item.getPublisher());

                newsRepository.save(news);
            }
            System.out.println("Page " + page + " 저장 완료 (" + body.getData().size() + "건)");
            return body.getTotal_pages(); // page == 1일 때만 유효
        }

        return 1; // fallback
    }

}
