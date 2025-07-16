package Baemin.News_Deliver.Domain.HotTopic.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotTopicResponseDTO {
    Long topicRank;
    String keyword;
    Long keywordCount;
    LocalDateTime topicDate;
}

