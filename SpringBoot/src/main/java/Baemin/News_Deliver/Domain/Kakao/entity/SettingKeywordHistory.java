package Baemin.News_Deliver.Domain.Kakao.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "setting_keyword_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SettingKeywordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_keyword", nullable = false)
    private String settingKeyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id", nullable = false, foreignKey = @ForeignKey(name = "fk_setting_keyword_history_history"))
    private History history;
}
