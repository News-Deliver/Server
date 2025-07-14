package Baemin.News_Deliver.Domain.SubServices.MoreNews.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MoreNewsDTO {
    private String title;
    private String summary;
    private String contentUrl;
    private LocalDateTime publishedAt;
}