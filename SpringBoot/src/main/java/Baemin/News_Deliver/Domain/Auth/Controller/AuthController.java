package Baemin.News_Deliver.Domain.Auth.Controller;

import Baemin.News_Deliver.Domain.Auth.Service.AuthService;
import Baemin.News_Deliver.Domain.Auth.dto.TokenResponse;
import Baemin.News_Deliver.Domain.Auth.dto.UserResponse;
import Baemin.News_Deliver.Global.ResponseObject.ApiResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "인증 관리", description = "사용자 인증 및 JWT 토큰 관리 API")
public class AuthController {

    private final AuthService authService;

    /**
     * 토큰 갱신 API
     */
    @PostMapping("/refresh")
    @Operation(summary = "JWT 토큰 갱신", description = "리프레시 토큰으로 새로운 액세스 토큰 발급")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "400", description = "리프레시 토큰이 제공되지 않음"),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰이 유효하지 않음")
    })
    public ResponseEntity<ApiResponseWrapper<TokenResponse>> refreshToken(
            @RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseWrapper<>(null, "Refresh token이 필요합니다"));
        }

        TokenResponse tokenResponse = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(
                new ApiResponseWrapper<>(tokenResponse, "토큰 갱신 성공")
        );
    }

    /**
     * 로그아웃 API
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 사용자 로그아웃 및 토큰 무효화")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
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
    @Operation(summary = "현재 사용자 정보 조회", description = "인증된 사용자의 기본 정보 조회")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponseWrapper<UserResponse>> getCurrentUser(
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponseWrapper<>(null, "인증되지 않은 사용자"));
        }

        String kakaoId = authentication.getName();
        UserResponse userResponse = authService.getCurrentUser(kakaoId);

        //dgdfgfddfg
        return ResponseEntity.ok(
                new ApiResponseWrapper<>(userResponse, "사용자 정보 조회 성공")
        );
    }
//dfgdfgdfgdfgdfgdfg
    /**
     * 로그인 상태 확인 API
     */
    @GetMapping("/status")
    @Operation(summary = "로그인 상태 확인", description = "현재 사용자의 로그인 상태 확인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 확인 완료")
    })
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