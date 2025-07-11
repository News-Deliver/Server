package Baemin.News_Deliver.Domain.Mypage.Repository;

import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import Baemin.News_Deliver.Domain.Mypage.Entity.SettingBlockKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingBlockKeywordRepository extends JpaRepository<SettingBlockKeyword, Long> {

    // 특정 설정의 제외 키워드 목록 조회
    List<SettingBlockKeyword> findBySetting(Setting setting);

    // 특정 설정의 제외 키워드 개수 조회
    long countBySetting(Setting setting);

    // 특정 설정의 모든 제외 키워드 삭제
    void deleteBySetting(Setting setting);

    // 특정 제외 키워드를 가진 모든 설정 조회
    List<SettingBlockKeyword> findBySettingKeyword(String settingKeyword);
}