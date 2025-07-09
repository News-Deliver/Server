package Baemin.News_Deliver.Global.News.JPAINSERT.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false)
    private String summary;

    @Column(name = "content_url", nullable = false, length = 255)
    private String contentUrl;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @Column(nullable = false)
    private Boolean send = false;

    @Column(nullable = false, length = 100)
    private String sections;

    @Column(nullable = false, length = 255)
    private String publisher;
}