package Baemin.News_Deliver.Domain.Mypage.Repository;

import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import Baemin.News_Deliver.Domain.Mypage.Entity.SettingKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * SettingKeyword 엔티티용 JPA Repository
 *
 * <p>설정에 연결된 키워드 데이터를 삭제할 수 있습니다.</p>
 */
@Repository
public interface SettingKeywordRepository extends JpaRepository<SettingKeyword, Long> {

    /**
     * 특정 설정에 속한 키워드 전체 삭제
     *
     * @param setting 대상 설정
     */
    @Modifying
    @Query("DELETE FROM SettingKeyword sk WHERE sk.setting = :setting")
    void deleteBySetting(@Param("setting") Setting setting);
}