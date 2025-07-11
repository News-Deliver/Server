package Baemin.News_Deliver.Domain.Mypage.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "setting_keyword")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettingKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "setting_keyword", nullable = false)
    private String settingKeyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_id", nullable = false)
    private Setting setting;

    @Builder
    public SettingKeyword(String settingKeyword, Setting setting) {
        this.settingKeyword = settingKeyword;
        this.setting = setting;
    }
}