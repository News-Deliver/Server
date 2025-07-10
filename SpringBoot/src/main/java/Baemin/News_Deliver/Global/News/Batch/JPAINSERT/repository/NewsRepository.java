package Baemin.News_Deliver.Global.News.Batch.JPAINSERT.repository;

import Baemin.News_Deliver.Global.News.Batch.JPAINSERT.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long> {
}
