package Baemin.News_Deliver.Domain.Mypage.Repository;

import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import Baemin.News_Deliver.Domain.Mypage.Entity.SettingBlockKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingBlockKeywordRepository extends JpaRepository<SettingBlockKeyword, Long> {
    @Modifying
    @Query("DELETE FROM SettingBlockKeyword sbk WHERE sbk.setting = :setting")
    void deleteBySetting(@Param("setting") Setting setting);
}