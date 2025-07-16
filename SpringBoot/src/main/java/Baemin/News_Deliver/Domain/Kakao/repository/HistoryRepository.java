package Baemin.News_Deliver.Domain.Kakao.repository;

import Baemin.News_Deliver.Domain.Kakao.dto.HistoryDTO;
import Baemin.News_Deliver.Domain.Kakao.entity.History;
import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import Baemin.News_Deliver.Global.News.Batch.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Long> {
    boolean existsBySettingAndNews(Setting setting, News news);

    /* 유저아이디를 통해 히스토리 리스트 반환 */
    List<History> findAllBySetting_User_Id(Long userId);

}
