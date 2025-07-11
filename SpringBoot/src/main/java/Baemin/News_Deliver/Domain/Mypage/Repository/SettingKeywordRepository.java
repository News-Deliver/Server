package Baemin.News_Deliver.Domain.Mypage.Repository;

import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import Baemin.News_Deliver.Domain.Mypage.Entity.SettingKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingKeywordRepository extends JpaRepository<SettingKeyword, Long> {

    // 특정 설정의 키워드 목록 조회
    List<SettingKeyword> findBySetting(Setting setting);

    // 특정 설정의 키워드 개수 조회
    long countBySetting(Setting setting);

    // 특정 설정의 모든 키워드 삭제
    void deleteBySetting(Setting setting);

    // 특정 키워드를 가진 모든 설정 조회 (스케줄러에서 사용  따른 분들과 토의후 변경)
    List<SettingKeyword> findBySettingKeyword(String settingKeyword);
}