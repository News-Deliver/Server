package Baemin.News_Deliver.Domain.Auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JWT 토큰 발급 및 갱신 API의 응답 DTO
 *
 * 사용자 로그인 성공 또는 토큰 갱신 요청 시 클라이언트에게 전달되는
 * JWT 액세스 토큰과 리프레시 토큰 정보를 포함합니다.
 * OAuth2 로그인 성공 핸들러와 토큰 갱신 API에서 사용됩니다.
 *
 * @see Baemin.News_Deliver.Domain.Auth.Controller.AuthController#refreshToken
 * @see Baemin.News_Deliver.Global.OAuth2.OAuth2LoginSuccessHandler
 */

 @Getter
@AllArgsConstructor
@Schema(description = "JWT 토큰 응답 정보")
public class TokenResponse {

    @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String accessToken;

    @Schema(description = "JWT 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String refreshToken;

    @Schema(description = "토큰 타입", example = "Bearer")
    private final String tokenType;

    @Schema(description = "액세스 토큰 만료 시간(초)", example = "1800")
    private final long expiresIn;
}