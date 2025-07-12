package Baemin.News_Deliver.Domain.Mypage.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "setting_block_keyword")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//현재 미개발
public class SettingBlockKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "setting_keyword", nullable = false)
    private String settingKeyword;  // 제외할 키워드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_id", nullable = false)
    private Setting setting;

    @Builder
    public SettingBlockKeyword(String settingKeyword, Setting setting) {
        this.settingKeyword = settingKeyword;
        this.setting = setting;
    }
}