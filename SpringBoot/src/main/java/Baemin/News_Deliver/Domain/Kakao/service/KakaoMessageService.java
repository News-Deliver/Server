package Baemin.News_Deliver.Domain.Kakao.service;

import Baemin.News_Deliver.Domain.Auth.Entity.User;
import Baemin.News_Deliver.Domain.Auth.Repository.UserRepository;
import Baemin.News_Deliver.Domain.Kakao.entity.History;
import Baemin.News_Deliver.Domain.Kakao.repository.HistoryRepository;
import Baemin.News_Deliver.Domain.Kakao.repository.NewsRepository;
import Baemin.News_Deliver.Domain.Mypage.DTO.SettingDTO;
import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import Baemin.News_Deliver.Domain.Mypage.Repository.SettingRepository;
import Baemin.News_Deliver.Domain.Mypage.service.SettingService;
import Baemin.News_Deliver.Global.Kakao.KakaoTokenProvider;
import Baemin.News_Deliver.Global.News.Batch.entity.News;
import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoMessageService {

    private final KakaoTokenProvider provider;
    private final RestTemplate restTemplate = new RestTemplate();
    private final KakaoNewsService newsService;
    private final SettingService settingService;
    private final NewsRepository newsRepository;
    private final SettingRepository settingRepository;
    private final HistoryRepository historyRepository;

    private static final String KAKAO_SEND_TOME_URL = "https://kapi.kakao.com/v2/api/talk/memo/send";

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;


    /**
     * 현재 로그인한 카카오 유저의 Access Token을 가져옴
     */
    public String getKakaoUserAccessToken(String refreshAccessToken, Long userId) {

        //유저의 accesstoken을 가져 올 것
        String accessToken = provider.refreshAccessToken(refreshAccessToken);
        if (accessToken == null || accessToken.isEmpty()) {
            throw new RuntimeException("Access Token을 가져올 수 없습니다.");
        }

        getNewsEsDocumentList(userId);
        return accessToken;
    }

    /**
     * 메시지 전송 메서드
     */
    public boolean sendKakaoMessage(String refreshAccessToken, Long userId) {
        try {
            String accessToken = getKakaoUserAccessToken(refreshAccessToken, userId);

            List<NewsEsDocument> newsList = getNewsEsDocumentList(userId);
            if (newsList == null) return false;

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Bearer " + accessToken);

            // 템플릿 설정(ES로 검색한 뉴스 리스트 넘겨받음)
            Map<String, String> templateArgs = createTemplateData(newsList);

            // JSON 문자열로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            String templateArgsJson = objectMapper.writeValueAsString(templateArgs);

            // 요청 파라미터 구성
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("template_id", "122080");
            params.add("template_args", templateArgsJson);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(KAKAO_SEND_TOME_URL, entity, String.class);

            log.info("카카오 메시지 전송 응답: {}", response.getBody());
            return response.getStatusCode() == HttpStatus.OK;

        } catch (Exception e) {
            log.error("카카오 메시지 전송 실패: ", e);
            return false;
        }
    }

    /**
     * 사용자의 키워드를 바탕으로 뉴스를 검색하여 리스트로 반환하는 메서드입니다.
     *
     * @return NewsEsDocument의 리스트
     */
    private List<NewsEsDocument> getNewsEsDocumentList(Long userId) {

        //유저 정보를 기준으로 Settig값 가져오기
        List<SettingDTO> settings = settingService.getAllSettingsByUserId(userId);

        List<String> keywords = new ArrayList<>();
        List<String> blockKeywords = new ArrayList<>();

        for (SettingDTO setting : settings) {
            log.info("셋팅값 확인용 코드 : " + setting.getSettingKeywords());
            log.info("셋팅 제외 확인용 코드 : " + setting.getBlockKeywords());

            // 키워드리스트의 null 값 체크
            if (setting.getSettingKeywords() != null) {
                keywords.add(setting.getSettingKeywords().toString());
            }

            blockKeywords.add(setting.getBlockKeywords().toString());
        }

        if (keywords.isEmpty()) {
            log.error("설정된 키워드가 없습니다.");
            return null;
        }

        //키워드별 뉴스 검색
        List<NewsEsDocument> newsList = newsService.searchNews(keywords, blockKeywords);

        log.info("검색된 뉴스 수: {}", newsList.size());
        newsList.forEach(n -> log.info("뉴스: {} - {}", n.getPublisher(), n.getSummary()));

        // 검색된 뉴스를 히스토리로 보내는 코드
        if (saveHistory(newsList, settings)) return null;
        return newsList;
    }

    private static Map<String, String> createTemplateData(List<NewsEsDocument> newsList) {

        log.info("뉴스 전체 리스트 확인용:" + newsList);
        Map<String, String> templateArgs = new HashMap<>();

        //메세지 5개 고정
        for (int i = 0; i < Math.min(5, newsList.size()); i++) {
            NewsEsDocument news = newsList.get(i);
            templateArgs.put("SUMMARY" + (i + 1), news.getSummary());
            templateArgs.put("PUBLISHER" + (i + 1), news.getPublisher());
        }
        return templateArgs;

    }

    private boolean saveHistory(List<NewsEsDocument> newsList, List<SettingDTO> settings) {
        if (newsList == null || newsList.isEmpty()) {
            log.warn("해당 키워드로 검색된 뉴스가 없습니다.");
            return false;
        }

        boolean saved = false;

        for (NewsEsDocument newsDoc : newsList) {
            News newsitem = newsRepository.findById(Long.parseLong(newsDoc.getId()))
                    .orElseThrow(() -> new RuntimeException("뉴스가 존재하지 않습니다: " + newsDoc.getId()));

            for (SettingDTO settingDTO : settings) {
                Setting setting = settingRepository.findById(settingDTO.getId())
                        .orElseThrow(() -> new RuntimeException("설정이 존재하지 않습니다: " + settingDTO.getId()));

                // 중복 저장 방지용 코드
                boolean exists = historyRepository.existsBySettingAndNews(setting, newsitem);
                if (exists) {
                    log.info("이미 저장된 뉴스입니다. (settingId={}, newsId={})", setting.getId(), newsitem.getId());
                    continue;
                }

                History history = History.builder()
                        .publishedAt(newsDoc.getPublished_at())
                        .setting(setting)
                        .news(newsitem)
                        .settingKeyword(String.join(",", settingDTO.getSettingKeywords()))
                        .blockKeyword(settingDTO.getBlockKeywords() != null ?
                                String.join(",", settingDTO.getBlockKeywords()) : null)
                        .build();

                historyRepository.save(history);
                saved = true;
            }
        }

        return saved;
    }

}