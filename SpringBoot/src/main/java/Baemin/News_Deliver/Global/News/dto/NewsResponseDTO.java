<<<<<<<< HEAD:SpringBoot/src/main/java/Baemin/News_Deliver/Global/News/JPAINSERT/dto/NewsResponseDTO.java
package Baemin.News_Deliver.Global.News.JPAINSERT.dto;
========
package Baemin.News_Deliver.Global.News.dto;
>>>>>>>> origin/dev:SpringBoot/src/main/java/Baemin/News_Deliver/Global/News/dto/NewsResponseDTO.java

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