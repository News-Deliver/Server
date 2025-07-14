package Baemin.News_Deliver.Domain.Auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO
 */
@Getter
@AllArgsConstructor
public class UserResponse {
    private final Long id;
    private final String kakaoId;
    private final LocalDateTime createdAt;
}