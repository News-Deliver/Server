package Baemin.News_Deliver.Domain.Kakao.repository;

import Baemin.News_Deliver.Domain.Kakao.dto.HistoryDTO;
import Baemin.News_Deliver.Domain.Kakao.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History, Long> {
}
