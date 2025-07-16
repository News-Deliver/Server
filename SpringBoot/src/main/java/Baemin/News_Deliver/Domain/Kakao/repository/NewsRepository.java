package Baemin.News_Deliver.Domain.Kakao.repository;

import Baemin.News_Deliver.Global.News.Batch.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long> {
}
