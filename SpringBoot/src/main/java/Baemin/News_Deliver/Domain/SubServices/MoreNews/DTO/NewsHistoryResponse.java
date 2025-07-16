package Baemin.News_Deliver.Domain.SubServices.MoreNews.DTO;


import Baemin.News_Deliver.Domain.Kakao.entity.History;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsHistoryResponse {

    private Long id;
    private String settingKeyword;
    private String blockKeyword;
    private LocalDateTime publishedAt;
    private String newsTitle;
    private String summary;
    private String content_url;

    public static NewsHistoryResponse from(History history) {
        return NewsHistoryResponse.builder()
                .id(history.getId())
                .settingKeyword(history.getSettingKeyword())
                .blockKeyword(history.getBlockKeyword())
                .publishedAt(history.getPublishedAt())
                .newsTitle(history.getNews().getTitle())           // News 엔티티의 title
                .summary(history.getNews().getSummary())           // News 엔티티의 summary
                .content_url(history.getNews().getContentUrl())    // News 엔티티의 URL
                .build();
    }
}
