package Baemin.News_Deliver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
@EnableCaching
public class NewsDeliverApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewsDeliverApplication.class, args);
	}

}
