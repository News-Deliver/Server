package Baemin.News_Deliver.Domain.Mypage.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "setting_keyword")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettingKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_keyword", nullable = false)
    private String settingKeyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_id", nullable = false)
    private Setting setting;
}