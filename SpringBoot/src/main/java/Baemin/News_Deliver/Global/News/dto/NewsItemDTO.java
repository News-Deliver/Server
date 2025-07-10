<<<<<<<< HEAD:SpringBoot/src/main/java/Baemin/News_Deliver/Global/News/JPAINSERT/dto/NewsItemDTO.java
package Baemin.News_Deliver.Global.News.JPAINSERT.dto;
========
package Baemin.News_Deliver.Global.News.dto;
>>>>>>>> origin/dev:SpringBoot/src/main/java/Baemin/News_Deliver/Global/News/dto/NewsItemDTO.java

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
