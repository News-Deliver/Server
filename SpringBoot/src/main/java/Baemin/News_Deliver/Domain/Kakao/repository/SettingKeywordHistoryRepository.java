package Baemin.News_Deliver.Domain.Kakao.repository;

import Baemin.News_Deliver.Domain.Kakao.entity.History;
import Baemin.News_Deliver.Domain.Kakao.entity.SettingKeywordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingKeywordHistoryRepository extends JpaRepository<SettingKeywordHistory, Long> {

    // 특정 히스토리에 포함된 키워드 목록 조회
    List<SettingKeywordHistory> findByHistory(History history);

    // 특정 히스토리 ID로 조회
    List<SettingKeywordHistory> findByHistoryId(Long historyId);

    // 특정 히스토리 내 특정 키워드 존재 여부 확인
    boolean existsByHistoryAndSettingKeyword(History history, String settingKeyword);

    // 특정 히스토리의 모든 키워드 삭제
    void deleteByHistory(History history);

}
