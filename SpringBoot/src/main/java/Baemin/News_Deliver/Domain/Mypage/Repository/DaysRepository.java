package Baemin.News_Deliver.Domain.Mypage.Repository;

import Baemin.News_Deliver.Domain.Mypage.Entity.Days;
import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DaysRepository extends JpaRepository<Days, Long> {

    // 특정 설정의 요일 목록 조회
    List<Days> findBySetting(Setting setting);

    // 특정 설정의 요일 개수 조회
    long countBySetting(Setting setting);

    // 특정 설정의 모든 요일 삭제
    void deleteBySetting(Setting setting);

    // 특정 요일에 해당하는 모든 설정 조회 (스케줄러에서 사용  /  다른 분들과 상의 후 변경)
    List<Days> findByDeliveryDays(String deliveryDays);
}