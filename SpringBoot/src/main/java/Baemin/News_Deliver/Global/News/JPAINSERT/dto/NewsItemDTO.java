package Baemin.News_Deliver.Global.News.JPAINSERT.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsItemDTO {
    private String id; // 외부 ID (BigInt로 변환 가능)
    private String sections;
    private String title;
    private String publisher;
    private String summary;
    private String content_url;
    private LocalDateTime published_at;
}
