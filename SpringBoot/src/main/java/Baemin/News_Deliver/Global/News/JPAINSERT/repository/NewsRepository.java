<<<<<<<< HEAD:SpringBoot/src/main/java/Baemin/News_Deliver/Global/News/JPAINSERT/repository/NewsRepository.java
package Baemin.News_Deliver.Global.News.JPAINSERT.repository;

import Baemin.News_Deliver.Global.News.JPAINSERT.entity.News;
========
package Baemin.News_Deliver.Global.News.repository;

import Baemin.News_Deliver.Global.News.entity.News;
>>>>>>>> origin/dev:SpringBoot/src/main/java/Baemin/News_Deliver/Global/News/repository/NewsRepository.java
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long> {
}
