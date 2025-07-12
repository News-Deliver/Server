package Baemin.News_Deliver.Domain.Auth.Controller;

import Baemin.News_Deliver.Domain.Auth.Service.AuthService;
import Baemin.News_Deliver.Global.ResponseObject.ApiResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 토큰 갱신 API
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseWrapper<AuthService.TokenResponse>> refreshToken(
            @RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseWrapper<>(null, "Refresh token이 필요합니다"));
        }

        AuthService.TokenResponse tokenResponse = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(
                new ApiResponseWrapper<>(tokenResponse, "토큰 갱신 성공")
        );
    }

    /**
     * 로그아웃 API
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseWrapper<String>> logout(Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponseWrapper<>(null, "인증되지 않은 사용자"));
        }

        String kakaoId = authentication.getName();
        authService.logout(kakaoId);

        return ResponseEntity.ok(
                new ApiResponseWrapper<>("로그아웃 성공", "로그아웃이 완료되었습니다")
        );
    }

    /**
     * 현재 사용자 정보 조회 API
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponseWrapper<AuthService.UserResponse>> getCurrentUser(
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponseWrapper<>(null, "인증되지 않은 사용자"));
        }

        String kakaoId = authentication.getName();
        AuthService.UserResponse userResponse = authService.getCurrentUser(kakaoId);

        return ResponseEntity.ok(
                new ApiResponseWrapper<>(userResponse, "사용자 정보 조회 성공")
        );
    }

    /**
     * 로그인 상태 확인 API
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponseWrapper<Map<String, Object>>> checkLoginStatus(
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.ok(
                    new ApiResponseWrapper<>(
                            Map.of("isLoggedIn", false),
                            "로그인되지 않은 상태"
                    )
            );
        }

        return ResponseEntity.ok(
                new ApiResponseWrapper<>(
                        Map.of(
                                "isLoggedIn", true,
                                "kakaoId", authentication.getName()
                        ),
                        "로그인된 상태"
                )
        );
    }
}