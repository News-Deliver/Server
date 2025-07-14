package Baemin.News_Deliver.Domain.Auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 정보 조회 API의 응답 DTO
 *
 * 현재 인증된 사용자의 기본 정보를 클라이언트에게 전달하는 데 사용됩니다.
 * 사용자 정보 조회 API(/api/auth/me)에서 반환되며,
 * 민감한 정보는 제외하고 필요한 기본 정보만을 포함합니다.
 *
 * @see Baemin.News_Deliver.Domain.Auth.Controller.AuthController#getCurrentUser
 * @see Baemin.News_Deliver.Domain.Auth.Entity.User
 */
@Getter
@AllArgsConstructor
public class UserResponse {
    private final Long id;
    private final String kakaoId;
    private final LocalDateTime createdAt;
}