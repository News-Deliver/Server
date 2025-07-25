package Baemin.News_Deliver.Domain.Kakao.Helper;

import Baemin.News_Deliver.Domain.Mypage.DTO.SettingDTO;
import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class KakaoMessageHelper {

    /**
     * 뉴스 리스트를 템플릿 전송용 파라미터(Map)로 변환하는 메서드(최대 5개)
     *
     * @param newsList 뉴스 리스트
     * @return 템플릿에 들어갈 파라미터 Map
     */
    public static Map<String, String> createTemplateData(List<NewsEsDocument> newsList) {

        log.info("뉴스 전체 리스트 확인:" + newsList);
        Map<String, String> templateArgs = new HashMap<>();

        /* 메세지 5개 고정 */
        for (int i = 0; i < Math.min(5, newsList.size()); i++) {
            NewsEsDocument news = newsList.get(i);
            templateArgs.put("TITLE" + (i + 1), news.getTitle());
            templateArgs.put("SUMMARY" + (i + 1), news.getSummary());
            templateArgs.put("PUBLISHER" + (i + 1), news.getPublisher());
            templateArgs.put("CONTENTURL" + (i + 1), "redirect?target=" + news.getContent_url());
            // templateArgs.put("CONTENTURL" + (i + 1), news.getContent_url());
        }
        return templateArgs;

    }

    /**
     * 현재 시간에 발송할 세팅이 있는지 확인하는 메서드
     *
     * @param currentSettings 현재 검사중인 세팅
     * @param nowTime 지금 시간
     */
    public static void checkCurrentSetting_Exist(List<SettingDTO> currentSettings, LocalTime nowTime) {

        if (currentSettings.isEmpty()) {
            log.info("현재 시간에 발송할 세팅이 없습니다: {}", nowTime);
        }
    }

    /**
     * 현재 시간 기준 뉴스 받아야 할 유저 리스트 필터링하는 메서드
     *
     * @param allSettings 모든 세팅값
     * @param nowTime 현재의 시간
     * @return 세팅 DTO
     */
    public static List<SettingDTO> filterCurrentSettings(List<SettingDTO> allSettings, LocalTime nowTime) {

        return allSettings.stream()
                .filter(setting -> {
                    LocalTime deliveryTime = setting.getDeliveryTime().toLocalTime().truncatedTo(ChronoUnit.MINUTES);
                    return deliveryTime.equals(nowTime);
                })
                .toList();
    }

}