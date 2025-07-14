package Baemin.News_Deliver.Domain.Auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JWT 토큰 응답 DTO
 */
@Getter
@AllArgsConstructor
public class TokenResponse {
    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final long expiresIn;
}