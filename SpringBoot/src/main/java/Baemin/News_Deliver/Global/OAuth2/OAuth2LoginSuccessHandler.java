package Baemin.News_Deliver.Global.OAuth2;

import Baemin.News_Deliver.Domain.Auth.Entity.Auth;
import Baemin.News_Deliver.Domain.Auth.Entity.User;
import Baemin.News_Deliver.Domain.Auth.Repository.AuthRepository;
import Baemin.News_Deliver.Domain.Auth.Repository.UserRepository;
import Baemin.News_Deliver.Global.JWT.JwtTokenProvider;
import Baemin.News_Deliver.Global.Redis.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final ObjectMapper objectMapper;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String kakaoId = oauth2User.getAttribute("id").toString();
        log.info("카카오 로그인 성공: kakaoId = {}", kakaoId);

        try {
            // 1. 카카오 리프레시 토큰 추출
            String kakaoRefreshToken = extractKakaoRefreshToken(authentication);

            // 2. User 테이블에 사용자 정보 저장 (없으면 생성)
            User user = findOrCreateUser(kakaoId);

            // 3. Auth 테이블에 카카오 리프레시 토큰 저장
            saveKakaoRefreshToken(user, kakaoRefreshToken);

            // 4. 우리 서비스 JWT 토큰 생성
            String ourAccessToken = jwtTokenProvider.generateAccessToken(kakaoId);
            String ourRefreshToken = jwtTokenProvider.generateRefreshToken(kakaoId);

            // 5. Redis에 우리 서비스 JWT 토큰 저장
            redisService.saveAccessToken(kakaoId, ourAccessToken);
            redisService.saveRefreshToken(kakaoId, ourRefreshToken);

            // 6. 프론트엔드로 리다이렉트 (토큰 정보 포함)
            String redirectUrl = createRedirectUrl(ourAccessToken, ourRefreshToken, user);
            response.sendRedirect(redirectUrl);

            log.info("로그인 처리 완료: kakaoId = {}, userId = {}", kakaoId, user.getId());

        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생: kakaoId = {}, error = {}", kakaoId, e.getMessage());
            sendErrorResponse(response, "로그인 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 카카오 리프레시 토큰 추출
     */
    private String extractKakaoRefreshToken(Authentication authentication) {
        try {
            OAuth2AuthorizedClient authorizedClient = authorizedClientService
                    .loadAuthorizedClient("kakao", authentication.getName());

            if (authorizedClient != null) {
                OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
                if (refreshToken != null) {
                    return refreshToken.getTokenValue();
                }
            }
        } catch (Exception e) {
            log.error("카카오 리프레시 토큰 추출 실패: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 사용자 조회 또는 생성
     */
    private User findOrCreateUser(String kakaoId) {
        return userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .kakaoId(kakaoId)
                            .build();
                    User savedUser = userRepository.save(newUser);
                    log.info("새 사용자 생성: kakaoId = {}, userId = {}", kakaoId, savedUser.getId());
                    return savedUser;
                });
    }

    /**
     * 카카오 리프레시 토큰 저장
     */
    private void saveKakaoRefreshToken(User user, String kakaoRefreshToken) {
        Optional<Auth> optionalAuth = authRepository.findByUser(user);

        if (optionalAuth.isPresent()) {
            // 기존 Auth 업데이트
            Auth auth = optionalAuth.get();
            auth.updateKakaoRefreshToken(kakaoRefreshToken);
            authRepository.save(auth);
            log.info("기존 사용자 토큰 업데이트: userId = {}", user.getId());
        } else {
            // 새 Auth 생성
            Auth newAuth = Auth.builder()
                    .user(user)
                    .kakaoRefreshToken(kakaoRefreshToken)
                    .build();
            authRepository.save(newAuth);
            log.info("새 사용자 토큰 저장: userId = {}", user.getId());
        }
    }

    /**
     * 리다이렉트 URL 생성
     */
    private String createRedirectUrl(String accessToken, String refreshToken, User user) {
        try {
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("accessToken", accessToken);
            tokenData.put("refreshToken", refreshToken);
            tokenData.put("tokenType", "Bearer");
            tokenData.put("expiresIn", 1800);
            tokenData.put("user", Map.of(
                    "id", user.getId(),
                    "kakaoId", user.getKakaoId()
            ));

            String tokenJson = objectMapper.writeValueAsString(tokenData);
            String encodedToken = java.net.URLEncoder.encode(tokenJson, "UTF-8");

            return "/?token=" + encodedToken;
        } catch (Exception e) {
            log.error("리다이렉트 URL 생성 실패: {}", e.getMessage());
            return "/?error=true";
        }
    }

    /**
     * 에러 응답 전송
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "LOGIN_FAILED");
        errorResponse.put("message", message);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}