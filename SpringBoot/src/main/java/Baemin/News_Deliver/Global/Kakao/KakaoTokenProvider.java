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

/**
 * 카카오 OAuth2 액세스 토큰 관리를 담당하는 서비스
 *
 * 카카오 API와의 연동을 통해 액세스 토큰 발급, 갱신, 검증을 처리합니다.
 * 저장된 리프레시 토큰을 사용하여 만료된 액세스 토큰을 자동으로 갱신하며,
 * 카카오 API 호출이 필요한 다른 서비스에서 사용할 수 있는 유효한 토큰을 제공합니다.
 *
 * 주요 기능:
 * - 사용자별 카카오 액세스 토큰 발급
 * - 리프레시 토큰을 통한 액세스 토큰 갱신
 * - 액세스 토큰 유효성 검증
 * - 카카오 API 오류 처리 및 예외 변환
 *
 * @see Auth
 * @see User
 * @see AuthRepository
 */
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
     * 카카오 ID로 액세스 토큰을 발급받습니다
     *
     * 사용자의 카카오 ID를 기반으로 데이터베이스에서 저장된 리프레시 토큰을 조회하고,
     * 이를 사용하여 카카오 API에서 새로운 액세스 토큰을 발급받습니다.
     * 다른 서비스에서 카카오 API 호출이 필요할 때 가장 많이 사용되는 메서드입니다.
     * 우리 팀이 사용하기 편하게 바꿔봤어요
     * 처리 과정:
     * 1. 카카오 ID로 사용자 조회
     * 2. 사용자의 인증 정보(리프레시 토큰) 조회
     * 3. 리프레시 토큰으로 액세스 토큰 발급
     *
     * @param kakaoId 액세스 토큰을 발급받을 사용자의 카카오 ID
     * @return 카카오 API 호출에 사용할 수 있는 유효한 액세스 토큰
     * @throws AuthException 사용자를 찾을 수 없는 경우 (ErrorCode.USER_NOT_FOUND)
     * @throws AuthException 카카오 토큰이 유효하지 않은 경우 (ErrorCode.KAKAO_TOKEN_INVALID)
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
            throw e;
        } catch (Exception e) {
            log.error("카카오 액세스 토큰 발급 중 예상치 못한 오류: kakaoId={}, error={}", kakaoId, e.getMessage());
            throw new AuthException(ErrorCode.KAKAO_TOKEN_INVALID);
        }
    }

    /**
     * User 엔티티로 액세스 토큰을 발급받습니다
     *
     * 이미 User 엔티티 객체를 가지고 있을 때 사용하는 편의 메서드입니다.
     * 내부적으로 User 객체에서 카카오 ID를 추출하여 메인 getAccessToken 메서드를 호출합니다.
     * user 엔티티로 접속을 할떄 id로 뽑아옵니다
     * @param user 액세스 토큰을 발급받을 사용자 엔티티
     * @return 카카오 API 호출에 사용할 수 있는 유효한 액세스 토큰
     * @throws AuthException 카카오 토큰 발급에 실패한 경우
     */
    public String getAccessToken(User user) {
        log.debug("User 객체로 토큰 발급 요청: userId={}", user.getId());
        return getAccessToken(user.getKakaoId());
    }

    /**
     * 리프레시 토큰으로 새로운 액세스 토큰을 발급받습니다
     *
     * 카카오 인증 서버에 직접 요청을 보내 리프레시 토큰을 사용하여
     * 새로운 액세스 토큰을 발급받는 핵심 메서드입니다.
     * OAuth2 표준에 따라 grant_type을 "refresh_token"으로 설정하여 요청합니다.
     *
     * 요청 파라미터:
     * - grant_type: "refresh_token"
     * - client_id: 카카오 앱의 클라이언트 ID
     * - client_secret: 카카오 앱의 클라이언트 시크릿
     * - refresh_token: 저장된 리프레시 토큰
     *
     * @param refreshToken 카카오에서 발급받은 리프레시 토큰
     * @return 새로 발급받은 액세스 토큰
     * @throws AuthException 토큰 갱신에 실패한 경우 (ErrorCode.KAKAO_TOKEN_REFRESH_FAILED)
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
     * 카카오 액세스 토큰의 유효성을 검증합니다
     *
     * 카카오 사용자 정보 조회 API(/v2/user/me)를 호출하여
     * 주어진 액세스 토큰이 유효한지 확인합니다.
     * API 호출이 성공하면 토큰이 유효한 것으로 판단합니다.
     *
     * 이 메서드는 예외를 발생시키지 않고 boolean 값으로 결과를 반환하여,
     * 토큰 유효성을 간단히 확인하고 싶을 때 사용할 수 있는 헬퍼 메서드입니다.
     *
     * @param accessToken 검증할 카카오 액세스 토큰
     * @return 토큰 유효성 여부 (true: 유효, false: 무효)
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