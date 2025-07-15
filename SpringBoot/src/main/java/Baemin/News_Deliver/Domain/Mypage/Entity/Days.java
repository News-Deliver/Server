package Baemin.News_Deliver.Domain.Mypage.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "days")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Days {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_id", nullable = false)
    private Setting setting;

    @Column(name = "delivery_day", nullable = false)
    private Integer deliveryDay; // 1(일요일) ~ 7(토요일)
}