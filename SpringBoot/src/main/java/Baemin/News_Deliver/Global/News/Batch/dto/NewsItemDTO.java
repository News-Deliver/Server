package Baemin.News_Deliver.Global.News.Batch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 외부 API 응답 및 내부 News Entity 처리를 위한 전역 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "뉴스 항목 DTO")
public class NewsItemDTO {

    @Schema(description = "뉴스 ID (외부 API 기준 ID)", example = "1234567890")
    private String id;

    @Schema(description = "뉴스 섹션 목록", example = "[\"politics\", \"economy\"]")
    private List<String> sections;

    @Schema(description = "뉴스 제목", example = "라브로프, 북한 방문 중 군사협력 언급")
    private String title;

    @Schema(description = "뉴스 제공 언론사", example = "연합뉴스")
    private String publisher;

    @Schema(description = "뉴스 요약", example = "라브로프 러시아 외무장관이 북한과의 군사협력 강화 가능성을 언급했다.")
    private String summary;

    @Schema(description = "뉴스 원문 URL", example = "https://news.example.com/articles/123456")
    private String content_url;

    @Schema(description = "뉴스 발행 일시 (KST 기준)", example = "2025-07-15T09:00:00")
    private LocalDateTime published_at;
}