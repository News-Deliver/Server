package Baemin.News_Deliver.Domain.Mypage.Repository;

import Baemin.News_Deliver.Domain.Auth.Entity.User;
import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {

    // 사용자별 설정 목록 조회
    List<Setting> findByUser(User user);

    // 사용자별 설정 개수 조회 (최대 3개 제한 체크용)
    long countByUser(User user);

    // 사용자별 설정 존재 여부 확인
    boolean existsByUser(User user);
}