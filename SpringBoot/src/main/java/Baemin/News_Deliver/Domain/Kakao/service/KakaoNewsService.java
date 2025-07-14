package Baemin.News_Deliver.Domain.Kakao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoNewsService {

    // 임시 뉴스 데이터 (추후 엘라스틱서치로 대체)
    private static final List<NewsData> SAMPLE_NEWS = List.of(
            new NewsData("연합뉴스", "정부, 2025년 경제성장률 목표 2.6%로 설정... 내수 회복에 집중"),
            new NewsData("조선일보", "삼성전자, 3분기 영업이익 전년 대비 277% 증가... 반도체 회복세"),
            new NewsData("중앙일보", "서울 아파트 평균 매매가 12억 돌파... 전월 대비 0.8% 상승"),
            new NewsData("한국경제", "카카오페이, 해외 간편결제 서비스 확대... 동남아 3개국 진출"),
            new NewsData("매일경제", "현대차, 전기차 전용 플랫폼 기반 신모델 3종 연내 출시 예정")
    );

    /**
     * 엘라스틱서치에서 랜덤 뉴스 1개 조회 (임시 구현)
     */
    public String getRandomNewsMessage() {
        try {
            // 랜덤으로 뉴스 1개 선택 (추후 엘라스틱서치 쿼리로 대체)
            Random random = new Random();
            NewsData selectedNews = SAMPLE_NEWS.get(random.nextInt(SAMPLE_NEWS.size()));

            // 뉴스 메시지 포맷 생성
            return formatSingleNewsMessage(selectedNews);

        } catch (Exception e) {
            log.error("뉴스 데이터 조회 중 오류 발생: ", e);
            return "📰 배민 뉴스 딜리버리\n\n뉴스 데이터를 불러오는 중 오류가 발생했습니다.\n다시 시도해 주세요.";
        }
    }

    /**
     * 엘라스틱서치에서 여러 뉴스 조회 (임시 구현)
     */
    public String getMultipleNewsMessage(int count) {
        try {
            // 요청한 개수만큼 뉴스 선택 (중복 방지)
            List<NewsData> selectedNews = selectRandomNews(count);

            // 뉴스 요약 메시지 생성
            return formatMultipleNewsMessage(selectedNews);

        } catch (Exception e) {
            log.error("다중 뉴스 데이터 조회 중 오류 발생: ", e);
            return "📰 배민 뉴스 딜리버리\n\n뉴스 데이터를 불러오는 중 오류가 발생했습니다.\n다시 시도해 주세요.";
        }
    }

    /**
     * 특정 개수만큼 랜덤 뉴스 선택 (중복 방지)
     */
    private List<NewsData> selectRandomNews(int count) {
        List<NewsData> selectedNews = new ArrayList<>();
        List<NewsData> availableNews = new ArrayList<>(SAMPLE_NEWS);
        Random random = new Random();

        for (int i = 0; i < Math.min(count, availableNews.size()); i++) {
            int index = random.nextInt(availableNews.size());
            selectedNews.add(availableNews.remove(index));
        }

        return selectedNews;
    }

    /**
     * 단일 뉴스 메시지 포맷 생성
     */
    private String formatSingleNewsMessage(NewsData news) {
        StringBuilder message = new StringBuilder();
        message.append("📰 배민 뉴스 딜리버리\n\n");
        message.append("🏢 발행처: ").append(news.getPublisher()).append("\n");
        message.append("📝 요약: ").append(news.getSummary()).append("\n\n");
        message.append("자세한 내용은 배민 뉴스 딜리버리 앱에서 확인하세요!");

        return message.toString();
    }

    /**
     * 다중 뉴스 메시지 포맷 생성
     */
    private String formatMultipleNewsMessage(List<NewsData> newsList) {
        StringBuilder message = new StringBuilder();
        message.append("📰 배민 뉴스 딜리버리 - 오늘의 주요 뉴스\n\n");

        // 뉴스 목록 생성
        for (int i = 0; i < newsList.size(); i++) {
            NewsData news = newsList.get(i);
            message.append(String.format("%d. [%s] %s\n\n",
                    i + 1, news.getPublisher(), news.getSummary()));
        }

        message.append("자세한 내용은 배민 뉴스 딜리버리 앱에서 확인하세요!");
        return message.toString();
    }

    /**
     * 뉴스 데이터 클래스
     */
    public static class NewsData {
        private final String publisher;
        private final String summary;

        public NewsData(String publisher, String summary) {
            this.publisher = publisher;
            this.summary = summary;
        }

        public String getPublisher() { return publisher; }
        public String getSummary() { return summary; }
    }
}