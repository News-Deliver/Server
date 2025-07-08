package Baemin.News_Deliver.Global.Batch.repository;

import Baemin.News_Deliver.Global.Batch.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long> {
}
