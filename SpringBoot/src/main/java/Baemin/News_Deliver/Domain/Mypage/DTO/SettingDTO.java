package Baemin.News_Deliver.Domain.Mypage.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 뉴스 배달 설정 DTO
 *
 * <p>뉴스 배달 시간, 설정 기간, 키워드, 차단 키워드, 요일 정보 등을 포함하여
 * 사용자의 맞춤 설정을 저장하거나 수정할 때 사용되는 DTO입니다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 맞춤 뉴스 설정 정보")
public class SettingDTO {

    @Schema(description = "설정 ID (수정 시 사용)", example = "3")
    private Long id;

    @Schema(description = "뉴스 배달 시간", example = "2025-07-15T08:30:00")
    private LocalDateTime deliveryTime;

    @Schema(description = "설정 시작일", example = "2025-07-10T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "설정 종료일", example = "2025-08-10T00:00:00")
    private LocalDateTime endDate;

    @Schema(description = "사용자 ID", example = "42")
    private Long userId;

    @Schema(description = "뉴스를 받고 싶은 키워드 목록", example = "[\"정치\", \"경제\"]")
    private List<String> settingKeywords;

    @Schema(description = "받고 싶지 않은 차단 키워드 목록", example = "[\"사고\", \"범죄\"]")
    private List<String> blockKeywords;

    @Schema(description = "뉴스를 받고 싶은 요일 목록 (0:일 ~ 6:토)", example = "[1, 3, 5]")
    private List<Integer> days;
}