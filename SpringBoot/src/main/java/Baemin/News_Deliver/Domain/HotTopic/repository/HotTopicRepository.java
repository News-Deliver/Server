package Baemin.News_Deliver.Domain.HotTopic.repository;

import Baemin.News_Deliver.Domain.HotTopic.entity.HotTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HotTopic 엔티티를 위한 JPA 리포지토리
 *
 * <p>핫토픽 데이터를 저장/조회하기 위한 Repository로,
 * 전날 기준으로 상위 10개의 키워드를 조회하는 쿼리 메서드를 제공합니다.</p>
 *
 * 주요 메서드:
 * <ul>
 *   <li>{@code findTop10ByTopicDateBetweenOrderByTopicRankAsc()}:
 *       어제 날짜 범위 내에서 랭킹 순으로 정렬된 10개의 핫토픽 조회</li>
 * </ul>
 */
public interface HotTopicRepository extends JpaRepository<HotTopic, Long> {

    /**
     * 어제 날짜 기준 상위 10개의 핫토픽 조회
     *
     * @param start 어제 00:00:00
     * @param end 어제 23:59:59
     * @return 핫토픽 리스트 (최대 10개)
     */
    List<HotTopic> findTop10ByTopicDateBetweenOrderByTopicRankAsc(LocalDateTime start, LocalDateTime end);
}