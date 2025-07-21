package Baemin.News_Deliver.Domain.Kakao.repository;

import Baemin.News_Deliver.Domain.Kakao.dto.HistoryDTO;
import Baemin.News_Deliver.Domain.Kakao.entity.History;
import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import Baemin.News_Deliver.Global.News.Batch.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 유저에게 카카오톡으로 뉴스를 전달하면 기록되는
 * 뉴스 전송 기록(History) 엔티티에 대한 JPA 리포지토리 인터페이스입니다.
 */
public interface HistoryRepository extends JpaRepository<History, Long> {
    boolean existsBySettingAndNews(Setting setting, News news);

    /**
     * 유저아이디를 통해 히스토리 리스트 반환
     *
     * @param userId 유저의 고유 번호
     * @return 히스토리 리스트
     */
    List<History> findAllBySetting_User_Id(Long userId);

    /**
     * 히스토리 고유 번호를 통한 히스토리 객체 반환
     *
     * @param historyId 히스토리 고유 번호
     * @return 히스토리 객체
     */
    Optional<History> findById(Long historyId);

}
