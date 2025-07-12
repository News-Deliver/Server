package Baemin.News_Deliver.Global.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${redis.session1.host}")
    private String session1Host;

    @Value("${redis.session1.port}")
    private int session1Port;

    @Value("${redis.session2.host}")
    private String session2Host;

    @Value("${redis.session2.port}")
    private int session2Port;

    // Redis Session1 (JWT 토큰 저장용) - Primary
    @Bean(name = "redisSession1ConnectionFactory")
    @Primary
    public RedisConnectionFactory redisSession1ConnectionFactory() {
        return new LettuceConnectionFactory(session1Host, session1Port);
    }

    @Bean(name = "redisSession1Template")
    @Primary
    public RedisTemplate<String, Object> redisSession1Template() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisSession1ConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }

    // Redis Session2 (JWT 토큰 백업용)
    @Bean(name = "redisSession2ConnectionFactory")
    public RedisConnectionFactory redisSession2ConnectionFactory() {
        return new LettuceConnectionFactory(session2Host, session2Port);
    }

    @Bean(name = "redisSession2Template")
    public RedisTemplate<String, Object> redisSession2Template() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisSession2ConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }
}