package Baemin.News_Deliver.Global.Kakao;

import Baemin.News_Deliver.Domain.Auth.Entity.Auth;
import Baemin.News_Deliver.Domain.Auth.Entity.User;
import Baemin.News_Deliver.Domain.Auth.Exception.AuthException;
import Baemin.News_Deliver.Domain.Auth.Repository.AuthRepository;
import Baemin.News_Deliver.Domain.Auth.Repository.UserRepository;
import Baemin.News_Deliver.Global.Exception.ErrorCode;
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
        log.info("카카오 액세스 토큰 발급 요청: kakaoId={}", kakaoId);

        try {
            // 1. 사용자 조회
            User user = userRepository.findByKakaoId(kakaoId)
                    .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));

            log.debug("사용자 조회 완료: userId={}", user.getId());

            // 2. Auth 정보 조회
            Auth auth = authRepository.findByUser(user)
                    .orElseThrow(() -> new AuthException(ErrorCode.KAKAO_TOKEN_INVALID));

            log.debug("Auth 정보 조회 완료: refreshToken 존재={}", auth.getKakaoRefreshToken() != null);

            // 3. 리프레시 토큰으로 액세스 토큰 발급
            String accessToken = refreshAccessToken(auth.getKakaoRefreshToken());

            log.info("카카오 액세스 토큰 발급 성공: kakaoId={}", kakaoId);
            return accessToken;

        } catch (AuthException e) {
            log.error("카카오 액세스 토큰 발급 실패: kakaoId={}, errorCode={}", kakaoId, e.getErrorcode().getErrorCode());
            throw e; // AuthException은 그대로 재던지기
        } catch (Exception e) {
            log.error("카카오 액세스 토큰 발급 중 예상치 못한 오류: kakaoId={}, error={}", kakaoId, e.getMessage());
            throw new AuthException(ErrorCode.KAKAO_TOKEN_INVALID);
        }
    }

    /**
     * User 객체로 액세스 토큰 발급
     * User 객체를 이미 가지고 있을 때 사용
     */
    public String getAccessToken(User user) {
        log.debug("User 객체로 토큰 발급 요청: userId={}", user.getId());
        return getAccessToken(user.getKakaoId());
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 발급 (핵심 로직)
     */
    public String refreshAccessToken(String refreshToken) {
        log.info("카카오 토큰 갱신 API 호출 시작: url={}", KAKAO_TOKEN_URL);

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

            log.debug("카카오 API 요청 파라미터 설정 완료: client_id={}", kakaoClientId);

            // HTTP 요청 생성
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            // 카카오 토큰 API 호출
            log.debug("카카오 토큰 API 호출 중...");
            ResponseEntity<Map> response = restTemplate.postForEntity(KAKAO_TOKEN_URL, request, Map.class);

            // 응답 상태 로깅
            log.info("카카오 API 응답 수신: statusCode={}", response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                log.debug("카카오 API 응답 키 확인: keys={}", responseBody.keySet());

                String accessToken = (String) responseBody.get("access_token");
                String tokenType = (String) responseBody.get("token_type");
                Integer expiresIn = (Integer) responseBody.get("expires_in");

                if (accessToken != null) {
                    log.info("카카오 액세스 토큰 발급 성공: tokenType={}, expiresIn={}초", tokenType, expiresIn);
                    return accessToken;
                } else {
                    log.error("카카오 응답에 access_token이 없음: responseBody={}", responseBody);
                    throw new AuthException(ErrorCode.KAKAO_TOKEN_REFRESH_FAILED);
                }
            } else {
                log.error("카카오 토큰 API 호출 실패: statusCode={}, responseBody={}",
                        response.getStatusCode(), response.getBody());
                throw new AuthException(ErrorCode.KAKAO_TOKEN_REFRESH_FAILED);
            }

        } catch (AuthException e) {
            log.error("카카오 토큰 갱신 실패: errorCode={}", e.getErrorcode().getErrorCode());
            throw e; // AuthException은 그대로 재던지기
        } catch (Exception e) {
            log.error("카카오 토큰 갱신 중 예상치 못한 오류 발생: error={}, errorType={}",
                    e.getMessage(), e.getClass().getSimpleName());
            throw new AuthException(ErrorCode.KAKAO_TOKEN_REFRESH_FAILED);
        }
    }

    /**
     * 카카오 액세스 토큰 유효성 검증
     * 필요시 사용할 수 있는 헬퍼 메서드
     */
    public boolean validateAccessToken(String accessToken) {
        log.debug("카카오 액세스 토큰 검증 시작");

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

            boolean isValid = response.getStatusCode() == HttpStatus.OK;

            log.info("카카오 토큰 검증 완료: isValid={}, statusCode={}", isValid, response.getStatusCode());

            if (isValid && response.getBody() != null) {
                Map<String, Object> userInfo = response.getBody();
                log.debug("카카오 사용자 정보 확인: kakaoId={}", userInfo.get("id"));
            }

            return isValid;

        } catch (Exception e) {
            log.warn("카카오 액세스 토큰 검증 실패: error={}", e.getMessage());
            return false;
        }
    }
}