package Baemin.News_Deliver.Domain.Kakao.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "setting_block_keyword_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SettingBlockKeywordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "block_keyword", nullable = false)
    private String blockKeyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id", nullable = false, foreignKey = @ForeignKey(name = "fk_block_keyword_history_history"))
    private History history;
}
