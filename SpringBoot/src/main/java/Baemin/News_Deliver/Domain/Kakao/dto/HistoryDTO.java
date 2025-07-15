package Baemin.News_Deliver.Domain.Kakao.dto;

import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryDTO {
    private String id;
    private LocalDateTime published_at;
    private String setting_id;
    private String news_id;
    private String settingKeyword;
    private String blockKeyword;

}