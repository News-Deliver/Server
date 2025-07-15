package Baemin.News_Deliver.Domain.Auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JWT 토큰 응답 DTO
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