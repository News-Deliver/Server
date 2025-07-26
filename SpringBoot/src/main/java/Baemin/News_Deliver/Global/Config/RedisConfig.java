package Baemin.News_Deliver.Global.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // Session Redis 1
    @Value("${redis.session1.host}")
    private String session1Host;

    @Value("${redis.session1.port}")
    private int session1Port;

    // Session Redis 2
    @Value("${redis.session2.host}")
    private String session2Host;

    @Value("${redis.session2.port}")
    private int session2Port;

    // Cache Redis
    @Value("${redis.cache.host}")
    private String cacheHost;

    @Value("${redis.cache.port}")
    private int cachePort;

    /* ---------------- Session1 Redis 설정 (Primary) ---------------- */

    @Primary
    @Bean(name = "redisSession1ConnectionFactory")
    public RedisConnectionFactory redisSession1ConnectionFactory() {
        return new LettuceConnectionFactory(session1Host, session1Port);
    }

    @Primary
    @Bean(name = "redisSession1Template")
    public RedisTemplate<String, Object> redisSession1Template() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisSession1ConnectionFactory());
        setStringSerializers(template);
        return template;
    }

    /* ---------------- Session2 Redis 설정 (Backup) ---------------- */

    @Bean(name = "redisSession2ConnectionFactory")
    public RedisConnectionFactory redisSession2ConnectionFactory() {
        return new LettuceConnectionFactory(session2Host, session2Port);
    }

    @Bean(name = "redisSession2Template")
    public RedisTemplate<String, Object> redisSession2Template() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisSession2ConnectionFactory());
        setStringSerializers(template);
        return template;
    }

    /* ---------------- Cache Redis 설정 (JSON 직렬화) ---------------- */

    @Bean(name = "redisCacheConnectionFactory")
    public RedisConnectionFactory redisCacheConnectionFactory() {
        return new LettuceConnectionFactory(cacheHost, cachePort);
    }

    @Bean(name = "redisCacheTemplate")
    public RedisTemplate<String, Object> redisCacheTemplate(
            @Qualifier("redisCacheConnectionFactory") RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        return template;
    }

    /* ---------------- 공통 ObjectMapper (LocalDate 등 지원) ---------------- */

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /* ---------------- 공통 문자열 직렬화 설정 ---------------- */

    private void setStringSerializers(RedisTemplate<String, Object> template) {
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
    }
}

// ======================= Deprecated =========================
// ======================= Deprecated =========================
// ======================= Deprecated =========================
// ======================= Deprecated =========================
// ======================= Deprecated =========================

//
//@Configuration
//public class RedisConfig {
//
//    @Value("${redis.session1.host}")
//    private String session1Host;
//
//    @Value("${redis.session1.port}")
//    private int session1Port;
//
//    @Value("${redis.session2.host}")
//    private String session2Host;
//
//    @Value("${redis.session2.port}")
//    private int session2Port;
//
//    // Redis Session1 (JWT 토큰 저장용) - Primary
//    @Bean(name = "redisSession1ConnectionFactory")
//    @Primary
//    public RedisConnectionFactory redisSession1ConnectionFactory() {
//        return new LettuceConnectionFactory(session1Host, session1Port);
//    }
//
//    @Bean(name = "redisSession1Template")
//    @Primary
//    public RedisTemplate<String, Object> redisSession1Template() {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(redisSession1ConnectionFactory());
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//        template.setHashValueSerializer(new StringRedisSerializer());
//        return template;
//    }
//
//    // Redis Session2 (JWT 토큰 백업용)
//    @Bean(name = "redisSession2ConnectionFactory")
//    public RedisConnectionFactory redisSession2ConnectionFactory() {
//        return new LettuceConnectionFactory(session2Host, session2Port);
//    }
//
//    @Bean(name = "redisSession2Template")
//    public RedisTemplate<String, Object> redisSession2Template() {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(redisSession2ConnectionFactory());
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//        template.setHashValueSerializer(new StringRedisSerializer());
//        return template;
//    }
//
//    @Bean(name = "redisCacheConnectionFactory")
//    public RedisConnectionFactory redisCacheConnectionFactory(
//            @Value("${redis.cache.host}") String cacheHost,
//            @Value("${redis.cache.port}") int cachePort
//    ) {
//        return new LettuceConnectionFactory(cacheHost, cachePort);
//    }
//
//    @Bean
//    public ObjectMapper objectMapper() {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        return mapper;
//    }
//
//    /**
//     * RedisTemplate (캐시 전용: 예, 뉴스 검색 결과 캐싱 등) — JSON 직렬화 기반
//     */
//    @Bean(name = "redisCacheTemplate")
//    public RedisTemplate<String, Object> redisCacheTemplate(
//            @Qualifier("redisCacheConnectionFactory") RedisConnectionFactory connectionFactory,
//            ObjectMapper objectMapper
//    ) {
//        GenericJackson2JsonRedisSerializer serializer =
//                new GenericJackson2JsonRedisSerializer(objectMapper);
//
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(serializer);
//        template.setHashKeySerializer(new StringRedisSerializer());
//        template.setHashValueSerializer(serializer);
//        return template;
//    }
//}