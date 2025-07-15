package Baemin.News_Deliver.Domain.Mypage.Repository;

import Baemin.News_Deliver.Domain.Auth.Entity.User;
import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {
    @Query("SELECT s FROM Setting s WHERE s.user = :user AND s.endDate > :now AND s.isDeleted = false")
    List<Setting> findActiveSettings(@Param("user") User user, @Param("now") LocalDateTime now);
}