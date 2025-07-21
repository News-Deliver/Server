package Baemin.News_Deliver.Domain.SubServices.MoreNews.DTO;


import Baemin.News_Deliver.Domain.Kakao.entity.History;
import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import Baemin.News_Deliver.Domain.SubServices.FeedBack.Entity.Feedback;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsHistoryResponse {

    private Long id;
    private String newsTitle;
    private String summary;
    private String content_url;
    private Long keywordReflection;
    private Long contentQuality;

    public static NewsHistoryResponse from(History history, Feedback feedback) {
        return NewsHistoryResponse.builder()
                .id(history.getId())
                .newsTitle(history.getNews().getTitle())           // News 엔티티의 title
                .summary(history.getNews().getSummary())           // News 엔티티의 summary
                .content_url(history.getNews().getContentUrl())    // News 엔티티의 URL
                .keywordReflection(feedback != null ? feedback.getKeywordReflection() : null)
                .contentQuality(feedback != null ? feedback.getContentQuality() : null)
                .build();
    }
}
