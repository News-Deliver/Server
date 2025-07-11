package Baemin.News_Deliver.Domain.Mypage.Entity;

import Baemin.News_Deliver.Domain.Auth.Entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "delivery_time", nullable = false)
    private LocalTime deliveryTime;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "setting", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Days> days;

    @OneToMany(mappedBy = "setting", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<SettingKeyword> keywords;

    @OneToMany(mappedBy = "setting", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<SettingBlockKeyword> blockKeywords;

    @Builder
    public Setting(LocalTime deliveryTime, LocalDateTime startDate,
                   LocalDateTime endDate, Boolean isDeleted, User user) {
        this.deliveryTime = deliveryTime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isDeleted = isDeleted;
        this.user = user;
    }
}