package Baemin.News_Deliver.Domain.Mypage.Entity;

import Baemin.News_Deliver.Domain.Auth.Entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_time", nullable = false)
    private LocalDateTime deliveryTime;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "setting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SettingKeyword> keywords;

    @OneToMany(mappedBy = "setting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SettingBlockKeyword> blockKeywords;

    @OneToMany(mappedBy = "setting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Days> days;
}