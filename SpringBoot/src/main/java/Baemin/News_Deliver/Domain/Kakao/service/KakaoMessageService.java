package Baemin.News_Deliver.Domain.Kakao.service;

import Baemin.News_Deliver.Domain.Auth.Repository.UserRepository;
import Baemin.News_Deliver.Global.Kakao.KakaoTokenProvider;
import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoMessageService {

    private final KakaoTokenProvider provider;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final KakaoNewsService newsService;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;

    private static final String KAKAO_SEND_TOME_URL = "https://kapi.kakao.com/v2/api/talk/memo/send";

    /**
     * 현재 로그인한 카카오 유저의 Access Token을 가져옴
     */
    public String getKakaoUserAccessToken() {
        //유저의 accesstoken을 가져 올 것
        String accessToken = provider.refreshAccessToken("i-s5VQgs2SMcQIFLG0HuCQ0dvAH6I8kpAAAAAgoNDV8AAAGYC_wG2FIZRy9oVvUS");

        if (accessToken == null || accessToken.isEmpty()) {
            throw new RuntimeException("Access Token을 가져올 수 없습니다.");
        }

        return accessToken;
    }

    /**
     * 메시지 전송 메서드
     */
    public boolean sendKakaoMessage() {
        try {
            String accessToken = getKakaoUserAccessToken();

            //임시 키워드 제공
            String keyword = "이재명";
            String blockKeyword = "";

            //키워드별 뉴스 검색
            List<NewsEsDocument> newsList = newsService.searchNews(keyword, blockKeyword);

            log.info("🔍 검색된 뉴스 수: {}", newsList.size());
            newsList.forEach(n -> log.info("📌 뉴스: {} - {}", n.getPublisher(), n.getSummary()));

            if (newsList.size() < 1) {
                log.warn("해당 키워드로 검색된 뉴스가 없습니다.");
                return false;
            }

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

    private static Map<String, String> createTemplateData(List<NewsEsDocument> newsList) {

        Map<String, String> templateArgs = new HashMap<>();

        //메세지 5개 고정
        for (int i = 0; i < Math.min(5, newsList.size()); i++) {
            NewsEsDocument news = newsList.get(i);
            templateArgs.put("SUMMARY" + (i + 1), news.getSummary());
            templateArgs.put("PUBLISHER" + (i + 1), news.getPublisher());
        }
        return templateArgs;

    }
}