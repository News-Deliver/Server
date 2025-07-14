package Baemin.News_Deliver.Domain.Kakao.repository;

import Baemin.News_Deliver.Domain.Kakao.entity.History;
import Baemin.News_Deliver.Domain.Kakao.entity.SettingBlockKeywordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingBlockKeywordHistoryRepository extends JpaRepository<SettingBlockKeywordHistory, Long> {

    // 특정 히스토리에 포함된 제외 키워드 목록 조회
    List<SettingBlockKeywordHistory> findByHistory(History history);

    // 특정 히스토리 ID 기준으로 조회
    List<SettingBlockKeywordHistory> findByHistoryId(Long historyId);

    // 특정 히스토리 내 특정 제외 키워드 존재 여부 확인
    boolean existsByHistoryAndBlockKeyword(History history, String blockKeyword);

    // 특정 히스토리의 제외 키워드 모두 삭제
    void deleteByHistory(History history);
}
