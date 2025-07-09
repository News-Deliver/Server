package Baemin.News_Deliver.Global.Redis;

import Baemin.News_Deliver.Global.Config.JwtConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisService {

    private final RedisTemplate<String, Object> redisSession1Template;
    private final RedisTemplate<String, Object> redisSession2Template;
    private final JwtConfig jwtConfig;

    public RedisService(@Qualifier("redisSession1Template") RedisTemplate<String, Object> redisSession1Template,
                        @Qualifier("redisSession2Template") RedisTemplate<String, Object> redisSession2Template,
                        JwtConfig jwtConfig) {
        this.redisSession1Template = redisSession1Template;
        this.redisSession2Template = redisSession2Template;
        this.jwtConfig = jwtConfig;
    }

    /**
     * Access Token 저장 (Primary + Backup)
     */
    public void saveAccessToken(String kakaoId, String accessToken) {
        String key = "access_token:" + kakaoId;
        long expiration = jwtConfig.getAccessTokenExpiration();

        try {
            // Primary Redis에 저장
            redisSession1Template.opsForValue().set(key, accessToken, expiration, TimeUnit.MILLISECONDS);

            // Backup Redis에 저장
            redisSession2Template.opsForValue().set(key, accessToken, expiration, TimeUnit.MILLISECONDS);

            log.info("Access Token 저장 완료: kakaoId = {}", kakaoId);
        } catch (Exception e) {
            log.error("Access Token 저장 실패: kakaoId = {}, error = {}", kakaoId, e.getMessage());
            throw new RuntimeException("토큰 저장 실패", e);
        }
    }

    /**
     * Refresh Token 저장 (Primary + Backup)
     */
    public void saveRefreshToken(String kakaoId, String refreshToken) {
        String key = "refresh_token:" + kakaoId;
        long expiration = jwtConfig.getRefreshTokenExpiration();

        try {
            // Primary Redis에 저장
            redisSession1Template.opsForValue().set(key, refreshToken, expiration, TimeUnit.MILLISECONDS);

            // Backup Redis에 저장
            redisSession2Template.opsForValue().set(key, refreshToken, expiration, TimeUnit.MILLISECONDS);

            log.info("Refresh Token 저장 완료: kakaoId = {}", kakaoId);
        } catch (Exception e) {
            log.error("Refresh Token 저장 실패: kakaoId = {}, error = {}", kakaoId, e.getMessage());
            throw new RuntimeException("토큰 저장 실패", e);
        }
    }

    /**
     * Access Token 조회 (Primary 우선, 실패 시 Backup)
     */
    public String getAccessToken(String kakaoId) {
        String key = "access_token:" + kakaoId;

        try {
            // Primary Redis에서 조회
            Object token = redisSession1Template.opsForValue().get(key);
            if (token != null) {
                return token.toString();
            }

            // Primary에서 실패 시 Backup Redis에서 조회
            token = redisSession2Template.opsForValue().get(key);
            return token != null ? token.toString() : null;

        } catch (Exception e) {
            log.error("Access Token 조회 실패: kakaoId = {}, error = {}", kakaoId, e.getMessage());
            return null;
        }
    }

    /**
     * Refresh Token 조회 (Primary 우선, 실패 시 Backup)
     */
    public String getRefreshToken(String kakaoId) {
        String key = "refresh_token:" + kakaoId;

        try {
            // Primary Redis에서 조회
            Object token = redisSession1Template.opsForValue().get(key);
            if (token != null) {
                return token.toString();
            }

            // Primary에서 실패 시 Backup Redis에서 조회
            token = redisSession2Template.opsForValue().get(key);
            return token != null ? token.toString() : null;

        } catch (Exception e) {
            log.error("Refresh Token 조회 실패: kakaoId = {}, error = {}", kakaoId, e.getMessage());
            return null;
        }
    }

    /**
     * 토큰 삭제 (Primary + Backup)
     */
    public void deleteTokens(String kakaoId) {
        String accessKey = "access_token:" + kakaoId;
        String refreshKey = "refresh_token:" + kakaoId;

        try {
            // Primary Redis에서 삭제
            redisSession1Template.delete(accessKey);
            redisSession1Template.delete(refreshKey);

            // Backup Redis에서 삭제
            redisSession2Template.delete(accessKey);
            redisSession2Template.delete(refreshKey);

            log.info("토큰 삭제 완료: kakaoId = {}", kakaoId);
        } catch (Exception e) {
            log.error("토큰 삭제 실패: kakaoId = {}, error = {}", kakaoId, e.getMessage());
        }
    }

    /**
     * Refresh Token 유효성 검증
     */
    public boolean validateRefreshToken(String kakaoId, String refreshToken) {
        String storedToken = getRefreshToken(kakaoId);
        return storedToken != null && storedToken.equals(refreshToken);
    }
}