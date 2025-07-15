package Baemin.News_Deliver.Domain.Mypage.DTO;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettingDTO {

    private Long id;
    private LocalDateTime deliveryTime;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Long userId;

    private List<String> settingKeywords;
    private List<String> blockKeywords;
    private List<Integer> days;
}
