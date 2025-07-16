package Baemin.News_Deliver.Global.News.Batch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 외부 뉴스 API 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "외부 뉴스 API 응답 전체 구조")
public class NewsResponseDTO {

    @Schema(description = "요청 결과 상세 상태 정보")
    private Detail detail;

    @Schema(description = "전체 뉴스 항목 개수", example = "127")
    private int total_items;

    @Schema(description = "전체 페이지 수", example = "13")
    private int total_pages;

    @Schema(description = "현재 페이지 번호", example = "1")
    private int page;

    @Schema(description = "한 페이지당 뉴스 개수", example = "10")
    private int page_size;

    @Schema(description = "뉴스 데이터 목록")
    private List<NewsItemDTO> data;

    /**
     * ✅ 응답 상세 상태 정보
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "요청 처리 결과에 대한 상태 메시지")
    public static class Detail {

        @Schema(description = "응답 메시지", example = "성공적으로 처리되었습니다.")
        private String message;

        @Schema(description = "응답 코드", example = "200")
        private String code;

        @Schema(description = "성공 여부", example = "true")
        private boolean ok;
    }
}