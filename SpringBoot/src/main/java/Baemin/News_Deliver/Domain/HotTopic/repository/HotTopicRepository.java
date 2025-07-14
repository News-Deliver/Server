package Baemin.News_Deliver.Domain.HotTopic.repository;

import Baemin.News_Deliver.Domain.HotTopic.entity.HotTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface HotTopicRepository extends JpaRepository<HotTopic, Long> {
    List<HotTopic> findTop10ByTopicDateBetweenOrderByTopicRankAsc(LocalDateTime start, LocalDateTime end);

}
