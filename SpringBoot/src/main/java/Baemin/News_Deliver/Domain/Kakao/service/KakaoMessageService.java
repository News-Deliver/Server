package Baemin.News_Deliver.Domain.Kakao.service;

import Baemin.News_Deliver.Domain.Auth.Repository.UserRepository;
import Baemin.News_Deliver.Global.Kakao.KakaoTokenProvider;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoMessageService {

    private final KakaoTokenProvider provider;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

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
        String accessToken = provider.refreshAccessToken("rDTkUyvQQ9jDku5OszEAOsdFoBwHZD1NAAAAAgoNIFoAAAGYB76pNlIZRy9oVvUS");

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

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Bearer " + accessToken);

            // 템플릿 설정
            Map<String, String> templateArgs = createTemplateData();

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

    private static Map<String, String> createTemplateData() {
        Map<String, String> templateArgs = new HashMap<>();
        templateArgs.put("SUMMARY1", "타이틀입니다.1");
        templateArgs.put("PUBLISHER1", "퍼블리셔입니다1");
        templateArgs.put("SUMMARY2", "타이틀입니다.2");
        templateArgs.put("PUBLISHER2", "퍼블리셔입니다2");
        templateArgs.put("SUMMARY3", "타이틀입니다.3");
        templateArgs.put("PUBLISHER3", "퍼블리셔입니다3");
        templateArgs.put("SUMMARY4", "타이틀입니다.4");
        templateArgs.put("PUBLISHER4", "퍼블리셔입니다4");
        templateArgs.put("SUMMARY5", "타이틀입니다.5");
        templateArgs.put("PUBLISHER5", "퍼블리셔입니다5");
        return templateArgs;
    }
}