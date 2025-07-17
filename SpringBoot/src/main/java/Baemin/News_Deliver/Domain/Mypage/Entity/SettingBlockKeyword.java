package Baemin.News_Deliver.Domain.Mypage.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "setting_block_keyword")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettingBlockKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "block_keyword", nullable = false)
    private String blockKeyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_id", nullable = false)
    private Setting setting;
}