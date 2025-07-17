package Baemin.News_Deliver.Domain.SubServices.FeedBack.Repository;

import Baemin.News_Deliver.Domain.SubServices.FeedBack.Entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /**
     * 히스토리 ID를 통해 Feedback 객체 반환
     *
     * @param historyId must not be {@literal null}. 히스토리 고유 번호
     * @return 피드백 객체
     */
    Optional<Feedback> findById(@NonNull Long historyId);

}
