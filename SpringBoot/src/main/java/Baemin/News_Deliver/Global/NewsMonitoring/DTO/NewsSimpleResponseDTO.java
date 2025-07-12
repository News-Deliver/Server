package Baemin.News_Deliver.Global.NewsMonitoring.DTO;

import Baemin.News_Deliver.Global.News.JPAINSERT.dto.NewsItemDTO;
import lombok.Data;

import java.util.List;

@Data
public class NewsSimpleResponseDTO {
    private int total_items;
    private int total_pages;
    private List<NewsItemDTO> data;
}
