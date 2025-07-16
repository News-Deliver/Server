package Baemin.News_Deliver.Domain.SubServices.MoreNews.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoreNewsResponse {

    private LocalDateTime published_at;
    private String title;
    private String summary;
    private String content_url;

}
