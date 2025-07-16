package Baemin.News_Deliver.Domain.SubServices.MoreNews.DTO;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupedNewsHistoryResponse {
    private Long settingId;
    private LocalDateTime publishedAt;
    private String settingKeyword;
    private String blockKeyword;
    private List<NewsHistoryResponse> newsList;
}
