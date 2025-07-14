package Baemin.News_Deliver.Domain.Kakao.service;

import Baemin.News_Deliver.Domain.Auth.Repository.UserRepository;
import Baemin.News_Deliver.Global.Kakao.KakaoTokenProvider;
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
        // 현재 유저 인증 확인 코드
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }

        // 유저의 카카오톡 아이디
        String kakaoId = authentication.getName();
        String accessToken = provider.getAccessToken(kakaoId);

        if (accessToken == null || accessToken.isEmpty()) {
            throw new RuntimeException("Access Token을 가져올 수 없습니다.");
        }

        return accessToken;
    }

    /**
     * 카카오 API를 통해 실제 메시지 전송
     */
    public boolean sendKakaoMessage(String message) {
        try {
            String accessToken = getKakaoUserAccessToken();

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Bearer " + accessToken);

            // 메시지 템플릿 생성 (텍스트 형태)
            String templateObject = createTextTemplate(message);

            // 요청 바디 설정
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("template_object", templateObject);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

            // API 호출
            ResponseEntity<String> response = restTemplate.postForEntity(KAKAO_SEND_TOME_URL, entity, String.class);

            log.info("카카오 메시지 전송 응답: {}", response.getBody());
            return response.getStatusCode() == HttpStatus.OK;

        } catch (Exception e) {
            log.error("카카오 메시지 전송 실패: ", e);
            return false;
        }
    }

    /**
     * 텍스트 메시지 템플릿 생성
     */
    private String createTextTemplate(String message) {
        Map<String, Object> templateObject = new HashMap<>();
        templateObject.put("object_type", "text");
        templateObject.put("text", message);
        templateObject.put("link", Map.of(
                "web_url", "https://your-app-domain.com",
                "mobile_web_url", "https://your-app-domain.com"
        ));

        // JSON 문자열로 변환
        return convertToJsonString(templateObject);
    }

    /**
     * Map을 JSON 문자열로 변환하는 간단한 메서드
     * 실제 프로젝트에서는 ObjectMapper를 사용하는 것이 좋습니다.
     */
    private String convertToJsonString(Map<String, Object> map) {
        try {
            // 간단한 JSON 생성 (실제로는 ObjectMapper 사용 권장)
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"object_type\":\"text\",");
            json.append("\"text\":\"").append(map.get("text")).append("\",");
            json.append("\"link\":{");
            json.append("\"web_url\":\"https://your-app-domain.com\",");
            json.append("\"mobile_web_url\":\"https://your-app-domain.com\"");
            json.append("}");
            json.append("}");
            return json.toString();
        } catch (Exception e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }
}