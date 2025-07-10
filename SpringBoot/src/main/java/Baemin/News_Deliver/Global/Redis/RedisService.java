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

        log.info("Access Token 저장 시작: kakaoId={}, expiration={}ms", kakaoId, expiration);

        try {
            // Primary Redis에 저장
            log.debug("Primary Redis에 Access Token 저장 시도: key={}", key);
            redisSession1Template.opsForValue().set(key, accessToken, expiration, TimeUnit.MILLISECONDS);
            log.debug("주 Redis Access Token 저장 성공");

            // Backup Redis에 저장
            redisSession2Template.opsForValue().set(key, accessToken, expiration, TimeUnit.MILLISECONDS);
            log.debug("백업 Redis Access Token 저장 성공");

            log.info("Access Token 저장 완료: kakaoId={}", kakaoId);
        } catch (Exception e) {
            log.error("Access Token 저장 실패: kakaoId={}, error={}, errorType={}",
                    kakaoId, e.getMessage(), e.getClass().getSimpleName());
            throw new RuntimeException("토큰 저장 실패", e);
        }
    }

    /**
     * Refresh Token 저장 (Primary + Backup)
     */
    public void saveRefreshToken(String kakaoId, String refreshToken) {
        String key = "refresh_token:" + kakaoId;
        long expiration = jwtConfig.getRefreshTokenExpiration();

        log.info("Refresh Token 저장 시작: kakaoId={}, expiration={}ms", kakaoId, expiration);

        try {
            // Primary Redis에 저장
            log.debug("Primary Redis에 Refresh Token 저장 시도: key={}", key);
            redisSession1Template.opsForValue().set(key, refreshToken, expiration, TimeUnit.MILLISECONDS);
            log.debug("주 Redis Refresh Token 저장 성공");

            // Backup Redis에 저장
            redisSession2Template.opsForValue().set(key, refreshToken, expiration, TimeUnit.MILLISECONDS);
            log.debug("백업 Redis Refresh Token 저장 성공");

            log.info("Refresh Token 저장 완료: kakaoId={}", kakaoId);
        } catch (Exception e) {
            log.error("Refresh Token 저장 실패: kakaoId={}, error={}, errorType={}",
                    kakaoId, e.getMessage(), e.getClass().getSimpleName());
            throw new RuntimeException("토큰 저장 실패", e);
        }
    }

    /**
     * Access Token 조회 (Primary 우선, 실패 시 Backup)
     */
    public String getAccessToken(String kakaoId) {
        String key = "access_token:" + kakaoId;

        log.debug("Access Token 조회 시작: kakaoId={}, key={}", kakaoId, key);

        try {
            // Primary Redis에서 조회
            Object token = redisSession1Template.opsForValue().get(key);
            if (token != null) {
                log.debug("Primary Redis에서 Access Token 조회 성공: kakaoId={}", kakaoId);
                return token.toString();
            } else {
                log.debug("Primary Redis에 Access Token 없음, Backup 시도: kakaoId={}", kakaoId);
            }

            // Primary에서 실패 시 Backup Redis에서 조회
            token = redisSession2Template.opsForValue().get(key);
            if (token != null) {
                log.warn("Backup Redis에서 Access Token 조회 성공 (Primary 없음): kakaoId={}", kakaoId);
                return token.toString();
            } else {
                log.warn("Access Token 조회 실패: kakaoId={}, primary=없음, backup=없음", kakaoId);
                return null;
            }

        } catch (Exception e) {
            log.error("Access Token 조회 실패: kakaoId={}, error={}, errorType={}",
                    kakaoId, e.getMessage(), e.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * Refresh Token 조회 (Primary 우선, 실패 시 Backup)
     */
    public String getRefreshToken(String kakaoId) {
        String key = "refresh_token:" + kakaoId;

        log.debug("Refresh Token 조회 시작: kakaoId={}, key={}", kakaoId, key);

        try {
            // Primary Redis에서 조회
            Object token = redisSession1Template.opsForValue().get(key);
            if (token != null) {
                log.debug("Primary Redis에서 Refresh Token 조회 성공: kakaoId={}", kakaoId);
                return token.toString();
            } else {
                log.debug("Primary Redis에 Refresh Token 없음, Backup 시도: kakaoId={}", kakaoId);
            }

            // Primary에서 실패 시 Backup Redis에서 조회
            token = redisSession2Template.opsForValue().get(key);
            if (token != null) {
                log.warn("Backup Redis에서 Refresh Token 조회 성공 (Primary 없음): kakaoId={}", kakaoId);
                return token.toString();
            } else {
                log.warn("Refresh Token 조회 실패: kakaoId={}, primary=없음, backup=없음", kakaoId);
                return null;
            }

        } catch (Exception e) {
            log.error("Refresh Token 조회 실패: kakaoId={}, error={}, errorType={}",
                    kakaoId, e.getMessage(), e.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * 토큰 삭제 (Primary + Backup)
     */
    public void deleteTokens(String kakaoId) {
        String accessKey = "access_token:" + kakaoId;
        String refreshKey = "refresh_token:" + kakaoId;

        log.info("토큰 삭제 시작: kakaoId={}", kakaoId);

        try {
            // Primary Redis에서 삭제
            log.debug("Primary Redis에서 토큰 삭제 시도");
            Boolean accessDeleted = redisSession1Template.delete(accessKey);
            Boolean refreshDeleted = redisSession1Template.delete(refreshKey);
            log.debug("Primary Redis 토큰 삭제 완료: accessDeleted={}, refreshDeleted={}",
                    accessDeleted, refreshDeleted);

            // Backup Redis에서 삭제
            redisSession2Template.delete(accessKey);
            redisSession2Template.delete(refreshKey);
            log.debug("Backup Redis 토큰 삭제 완료");

            log.info("토큰 삭제 완료: kakaoId={}", kakaoId);
        } catch (Exception e) {
            log.error("토큰 삭제 실패: kakaoId={}, error={}, errorType={}",
                    kakaoId, e.getMessage(), e.getClass().getSimpleName());
        }
    }

    /**
     * Refresh Token 유효성 검증
     */
    public boolean validateRefreshToken(String kakaoId, String refreshToken) {
        log.debug("Refresh Token 유효성 검증 시작: kakaoId={}", kakaoId);

        String storedToken = getRefreshToken(kakaoId);
        boolean isValid = storedToken != null && storedToken.equals(refreshToken);

        log.info("Refresh Token 유효성 검증 완료: kakaoId={}, isValid={}, storedExists={}",
                kakaoId, isValid, storedToken != null);

        return isValid;
    }
}