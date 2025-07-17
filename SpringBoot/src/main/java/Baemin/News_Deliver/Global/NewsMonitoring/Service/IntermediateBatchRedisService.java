package Baemin.News_Deliver.Global.NewsMonitoring.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class IntermediateBatchRedisService {

    /* Redis Session1 연결 */
    private final RedisTemplate<String, Object> redisSession1Template;
    public IntermediateBatchRedisService(@Qualifier("redisSession1Template") RedisTemplate<String, Object> redisSession1Template) {
        this.redisSession1Template = redisSession1Template;
    }

    private static final String BATCH_KEY_PREFIX = "IntermediateBatch:";

    /**
     * 중간 배치 실행 횟수를 조회 메서드
     *
     * @param section 중간 배치 횟수가 조회되는 섹션
     * @return 중간 배치 실행 여부
     */
    public int getBatchCount(String section) {

        // 중간 배치 Key
        String key = BATCH_KEY_PREFIX + section;

        try {

            Object value = redisSession1Template.opsForValue().get(key);
            int result = (value != null) ? Integer.parseInt(value.toString()) : 0;

            log.info("Redis에서 {}의 중간 배치 횟수 조회: {}", section, result);
            return result;
        } catch (Exception e) {

            log.error("Redis 중간 배치 횟수 조회 실패: section={}, error={}", section, e.getMessage());
            return 0;
        }
    }

    /**
     * 중간 배치 실행 횟수를 1 증가시키는 메서드
     * 만약 키가 없으면 1로 생성
     *  ttl = 1day
     *
     * @param section 뉴스 섹션
     */
    public void incrementBatchCount(String section) {

        String key = BATCH_KEY_PREFIX + section;

        try {

            redisSession1Template.opsForValue().increment(key);
            redisSession1Template.expire(key, Duration.ofHours(12));  // TTL = 12 시간

            log.info("Redis에 {} 섹션 중간 배치 횟수 증가", section);
        } catch (Exception e) {

            log.error("Redis 중간 배치 횟수 증가 실패: section={}, error={}", section, e.getMessage());
        }
    }

    /**
     * 자정이 되면 Redis 에 있는 IntermediateBatch 중간 배치 기록 삭제 메서드
     *
     * 스케줄러에 적용?
     * 서비스에 적용?
     */
    public void flushIntermediateBatchKeys() {
        try {
            // "IntermediateBatch:*" 패턴에 해당하는 키들을 모두 조회
            Set<String> keys = redisSession1Template.keys(BATCH_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisSession1Template.delete(keys);
                log.info("Redis에서 IntermediateBatch 관련 키들 삭제: {}", keys);
            } else {
                log.info("Redis에 IntermediateBatch 관련 키가 없음");
            }
        } catch (Exception e) {
            log.error("Redis IntermediateBatch 키 삭제 실패: error={}", e.getMessage());
        }
    }


    /**
     * 섹션 별 중간 배치 작업 횟수를 조회하는 메서드
     *
     * @return <섹션, 중간 배치 작업 수>
     */
    public Map<String, Integer> getAllBatchCountsForSections() {
        Map<String, Integer> batchCountMap = new HashMap<>();

        try {
            // 모든 섹션 정의
            String[] sections = {"politics", "economy", "society", "culture", "tech", "entertainment", "opinion"};

            // 각 섹션을 순회하며 중간 배치 작업 횟수 Map에 저장
            for (String section : sections) {
                String key = BATCH_KEY_PREFIX + section;
                Object value = redisSession1Template.opsForValue().get(key);
                int count = (value != null) ? Integer.parseInt(value.toString()) : 0;
                batchCountMap.put(section, count);
            }
            log.info("Redis에서 모든 섹션 중간 배치 횟수 조회 완료: {}", batchCountMap);
        } catch (Exception e) {
            log.error("Redis에서 섹션별 중간 배치 횟수 조회 실패: error={}", e.getMessage());
        }

        // 각 섹션 별 중간 배치 작업 반환
        return batchCountMap;
    }



}

