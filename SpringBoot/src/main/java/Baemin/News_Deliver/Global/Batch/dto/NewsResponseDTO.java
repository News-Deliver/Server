package Baemin.News_Deliver.Global.Batch.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsResponseDTO {
    private Detail detail;
    private int total_items;
    private int total_pages;
    private int page;
    private int page_size;
    private List<NewsItemDTO> data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {
        private String message;
        private String code;
        private boolean ok;
    }
}