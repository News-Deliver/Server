package Baemin.News_Deliver.Global.DataCachingSystem;

import Baemin.News_Deliver.Domain.SubServices.MoreNews.DTO.GroupedNewsHistoryResponse;
import Baemin.News_Deliver.Domain.SubServices.MoreNews.DTO.PageResponse;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisCacheManagerConfig {

    /**
     * 캐시 메니저 메서드 
     * 
     * 이를 통해, Spring Cache 어노테이션 사용 가능
     *
     * @param connectionFactory 레디스 서버와의 연결 객체
     * @return 캐시 결과 반환
     */
    @Bean(name = "redisCacheManager")
    public CacheManager redisCacheManager(
            @Qualifier("redisCacheConnectionFactory") RedisConnectionFactory connectionFactory) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO-8601 포맷 유지

        // ======================= Default =========================

        // Generic default (모든 캐시에 무난하게 적용될 기본값)
        GenericJackson2JsonRedisSerializer genericSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // TTL 30분
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericSerializer));

        // ======================= 히스토리 조회 캐시 설정 =========================

        JavaType pageResponseType = objectMapper.getTypeFactory()
                .constructParametricType(PageResponse.class, GroupedNewsHistoryResponse.class);

        Jackson2JsonRedisSerializer<PageResponse> getGroupedNewsHistory = new Jackson2JsonRedisSerializer<>(pageResponseType);
        getGroupedNewsHistory.setObjectMapper(objectMapper);

        RedisCacheConfiguration getGroupedNewsHistoryConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(6)) // TTL 6시간
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(getGroupedNewsHistory));

        // ======================= 뉴스 더보기 캐시 설정 =========================

        RedisCacheConfiguration moreNewsByHistoryConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(3)) // TTL 3일
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericSerializer));

        // ======================= 커스터 마이징 캐시 설정 =========================

        // 개발자 커스터마이징 캐시 생성
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("groupedNewsHistory", getGroupedNewsHistoryConfig);
        cacheConfigurations.put("moreNewsByHistory", moreNewsByHistoryConfig);


        // =======================내 히스토리 조회하기 캐싱 전략 =========================

        // 캐시 최종 반환
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig) // 기본 캐시
                .withInitialCacheConfigurations(cacheConfigurations) // 커스터마이즈 된 캐시
                .build();
    }
}
