package Baemin.News_Deliver.Domain.Auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO
 */
@Getter
@AllArgsConstructor
@Schema(description = "사용자 정보 응답")
public class UserResponse {

    @Schema(description = "사용자 고유 ID", example = "1")
    private final Long id;

    @Schema(description = "카카오 사용자 ID", example = "1234567890")
    private final String kakaoId;

    @Schema(description = "회원가입 일시", example = "2024-01-15T10:30:00")
    private final LocalDateTime createdAt;
}