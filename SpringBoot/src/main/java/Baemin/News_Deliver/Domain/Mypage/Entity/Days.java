package Baemin.News_Deliver.Domain.Mypage.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "days")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Days {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "delivery_days", nullable = false)
    private String deliveryDays;  // "MON", "TUE", "WED" 등

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_id", nullable = false)
    private Setting setting;

    @Builder
    public Days(String deliveryDays, Setting setting) {
        this.deliveryDays = deliveryDays;
        this.setting = setting;
    }
}