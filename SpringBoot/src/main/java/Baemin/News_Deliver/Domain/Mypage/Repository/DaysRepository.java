package Baemin.News_Deliver.Domain.Mypage.Repository;

import Baemin.News_Deliver.Domain.Mypage.Entity.Days;
import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DaysRepository extends JpaRepository<Days, Long> {
    @Modifying
    @Query("DELETE FROM Days d WHERE d.setting = :setting")
    void deleteBySetting(@Param("setting") Setting setting);
}