package Baemin.News_Deliver.Global.News.repository;

import Baemin.News_Deliver.Global.News.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long> {
}
