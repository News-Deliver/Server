package Baemin.News_Deliver.Domain.SubServices.FeedBack.Entity;

import Baemin.News_Deliver.Domain.Kakao.entity.History;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    private Long id; // History의 ID와 동일한 PK

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id") // History의 PK를 FK로 동시에 사용
    private History history;

    @Column(name = "keyword_reflection")
    private Long keywordReflection;

    @Column(name = "content_quality")
    private Long contentQuality;
}
