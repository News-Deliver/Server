package Baemin.News_Deliver.Global.Kakao;

import Baemin.News_Deliver.Domain.Auth.Entity.Auth;
import Baemin.News_Deliver.Domain.Auth.Entity.User;
import Baemin.News_Deliver.Domain.Auth.Repository.AuthRepository;
import Baemin.News_Deliver.Domain.Auth.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoTokenProvider {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";

    /**
     * 카카오 ID로 액세스 토큰 발급
     * 다른 개발자들이 가장 많이 사용할 메서드
     */
    public String getAccessToken(String kakaoId) {
        try {
            // 1. 사용자 조회
            User user = userRepository.findByKakaoId(kakaoId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            // 2. Auth 정보 조회
            Auth auth = authRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("카카오 토큰이 유효하지 않습니다"));

            // 3. 리프레시 토큰으로 액세스 토큰 발급
            return refreshAccessToken(auth.getKakaoRefreshToken());

        } catch (Exception e) {
            log.error("카카오 액세스 토큰 발급 실패: kakaoId = {}, error = {}", kakaoId, e.getMessage());
            throw new RuntimeException("카카오 토큰이 유효하지 않습니다");
        }
    }

    /**
     * User 객체로 액세스 토큰 발급
     * User 객체를 이미 가지고 있을 때 사용
     */
    public String getAccessToken(User user) {
        return getAccessToken(user.getKakaoId());
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 발급 (핵심 로직)
     */
    public String refreshAccessToken(String refreshToken) {
        try {
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 요청 파라미터 설정
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "refresh_token");
            params.add("client_id", kakaoClientId);
            params.add("client_secret", kakaoClientSecret);
            params.add("refresh_token", refreshToken);

            // HTTP 요청 생성
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            // 카카오 토큰 API 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(KAKAO_TOKEN_URL, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String accessToken = (String) responseBody.get("access_token");

                if (accessToken != null) {
                    log.info("카카오 액세스 토큰 발급 성공");
                    return accessToken;
                } else {
                    log.error("카카오 응답에 access_token이 없음: {}", responseBody);
                    throw new RuntimeException("카카오 토큰이 유효하지 않습니다");
                }
            } else {
                log.error("카카오 토큰 API 호출 실패: status = {}", response.getStatusCode());
                throw new RuntimeException("카카오 토큰이 유효하지 않습니다");
            }

        } catch (Exception e) {
            log.error("카카오 토큰 갱신 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("카카오 토큰이 유효하지 않습니다");
        }
    }

    /**
     * 카카오 액세스 토큰 유효성 검증
     * 필요시 사용할 수 있는 헬퍼 메서드
     */
    public boolean validateAccessToken(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);

            // 카카오 사용자 정보 API로 토큰 검증
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.debug("카카오 액세스 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }
}