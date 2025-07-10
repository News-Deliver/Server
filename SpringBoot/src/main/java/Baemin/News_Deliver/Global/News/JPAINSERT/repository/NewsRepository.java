package Baemin.News_Deliver.Global.News.JPAINSERT.repository;

import Baemin.News_Deliver.Global.News.JPAINSERT.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long> {
}
