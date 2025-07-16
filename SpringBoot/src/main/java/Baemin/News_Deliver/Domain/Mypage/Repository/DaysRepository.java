package Baemin.News_Deliver.Domain.Mypage.Repository;

import Baemin.News_Deliver.Domain.Mypage.Entity.Days;
import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Days 엔티티용 JPA Repository
 *
 * <p>설정에 연결된 요일 정보 데이터를 삭제할 수 있습니다.</p>
 */
@Repository
public interface DaysRepository extends JpaRepository<Days, Long> {

    /**
     * 특정 설정에 속한 요일 정보 전체 삭제
     *
     * @param setting 대상 설정
     */
    @Modifying
    @Query("DELETE FROM Days d WHERE d.setting = :setting")
    void deleteBySetting(@Param("setting") Setting setting);
}