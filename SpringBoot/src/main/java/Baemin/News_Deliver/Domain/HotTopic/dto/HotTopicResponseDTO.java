package Baemin.News_Deliver.Domain.HotTopic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 어제의 핫토픽 정보를 클라이언트에 응답하기 위한 DTO
 *
 * <p>각 항목은 Elasticsearch에서 추출된 인기 키워드이며, 랭킹, 등장 횟수, 기준 날짜 정보를 포함합니다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "어제의 핫토픽 응답 DTO")
public class HotTopicResponseDTO {

    @Schema(description = "핫토픽 순위 (1~10위)", example = "1")
    private Long topicRank;

    @Schema(description = "핫토픽 키워드", example = "윤석열")
    private String keyword;

    @Schema(description = "키워드 등장 횟수", example = "432")
    private Long keywordCount;

    @Schema(description = "기준 날짜 (KST 기준 어제)", example = "2025-07-14T00:00:00")
    private LocalDateTime topicDate;
}
