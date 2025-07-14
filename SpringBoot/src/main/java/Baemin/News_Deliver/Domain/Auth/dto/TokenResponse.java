package Baemin.News_Deliver.Domain.Auth.dto;

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
public class TokenResponse {
    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final long expiresIn;
}