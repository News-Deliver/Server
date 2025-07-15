package Baemin.News_Deliver.Domain.HotTopic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hot_topic")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_rank", nullable = false)
    private Long topicRank;

    @Column(name = "keyword", nullable = false, length = 255)
    private String keyword;

    @Column(name = "keyword_count", nullable = false)
    private Long keywordCount;

    @Column(name = "topic_date", nullable = false)
    private LocalDateTime topicDate;
}