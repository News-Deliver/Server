package Baemin.News_Deliver.Domain.SubServices.FeedBack.DTO;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedbackRequest {

    private Long historyId;
    private String item; // "keyword_reflection","content_quality"
    private Long feedbackValue; // "like" =1, "dislike" =-1

}
