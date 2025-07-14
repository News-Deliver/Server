package Baemin.News_Deliver.Global.Redis;

import Baemin.News_Deliver.Global.Config.JwtConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * JWT 토큰의 Redis 저장소 관리를 담당하는 서비스
 *
 * 이중화된 Redis 인스턴스(Primary + Backup)를 활용하여 JWT 토큰의 안정적인 저장과 조회를 제공합니다.
 * 액세스 토큰과 리프레시 토큰을 각각의 만료 시간에 맞춰 저장하며,
 * Primary Redis 장애 시 Backup Redis로 자동 대체하여 서비스 연속성을 보장합니다.
 *
 * 주요 기능:
 * - JWT 토큰 이중화 저장 (Primary + Backup Redis)
 * - 장애 상황 시 자동 Failover 조회
 * - 토큰 만료 시간 자동 관리
 * - 로그아웃 시 토큰 완전 삭제
 * - 리프레시 토큰 유효성 검증
 *
 * Redis 키 형식:
 * - 액세스 토큰: "access_token:{kakaoId}"
 * - 리프레시 토큰: "refresh_token:{kakaoId}"
 *
 * @see JwtConfig
 * @see RedisTemplate
 */
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
     * JWT 액세스 토큰을 이중화하여 저장합니다
     *
     * Primary Redis와 Backup Redis 모두에 액세스 토큰을 저장하여 데이터 안정성을 확보합니다.
     * 토큰은 JwtConfig에서 설정된 만료 시간(30분)에 따라 자동으로 만료됩니다.
     * Primary Redis 저장 실패 시에도 전체 작업이 실패하지 않도록 예외 처리를 수행합니다.
     *
     * 저장 키 형식: "access_token:{kakaoId}"
     *
     * @param kakaoId 토큰을 저장할 사용자의 카카오 ID
     * @param accessToken 저장할 JWT 액세스 토큰
     * @throws RuntimeException Redis 저장 작업이 완전히 실패한 경우
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
     * JWT 리프레시 토큰을 이중화하여 저장합니다
     *
     * Primary Redis와 Backup Redis 모두에 리프레시 토큰을 저장하여 데이터 안정성을 확보합니다.
     * 토큰은 JwtConfig에서 설정된 만료 시간(24시간)에 따라 자동으로 만료됩니다.
     * 리프레시 토큰은 액세스 토큰 갱신 시 유효성 검증에 사용됩니다.
     *
     * 저장 키 형식: "refresh_token:{kakaoId}"
     *
     * @param kakaoId 토큰을 저장할 사용자의 카카오 ID
     * @param refreshToken 저장할 JWT 리프레시 토큰
     * @throws RuntimeException Redis 저장 작업이 완전히 실패한 경우
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
     * JWT 액세스 토큰을 조회합니다 (Failover 지원)
     *
     * Primary Redis에서 먼저 토큰을 조회하고, 실패 시 자동으로 Backup Redis에서 조회합니다.
     * 이를 통해 Redis 인스턴스 장애 시에도 서비스 연속성을 보장합니다.
     * 두 Redis 모두에서 토큰을 찾을 수 없는 경우 null을 반환합니다.
     *
     * @param kakaoId 토큰을 조회할 사용자의 카카오 ID
     * @return 저장된 JWT 액세스 토큰, 토큰이 없거나 만료된 경우 null
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
     * JWT 리프레시 토큰을 조회합니다 (Failover 지원)
     *
     * Primary Redis에서 먼저 토큰을 조회하고, 실패 시 자동으로 Backup Redis에서 조회합니다.
     * 토큰 갱신 시 유효성 검증에 사용되며, 서비스 연속성을 위한 Failover 기능을 제공합니다.
     * 두 Redis 모두에서 토큰을 찾을 수 없는 경우 null을 반환합니다.
     *
     * @param kakaoId 토큰을 조회할 사용자의 카카오 ID
     * @return 저장된 JWT 리프레시 토큰, 토큰이 없거나 만료된 경우 null
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
     * 사용자의 모든 JWT 토큰을 삭제합니다
     *
     * 로그아웃 시 호출되어 사용자의 액세스 토큰과 리프레시 토큰을 모두 삭제합니다.
     * Primary Redis와 Backup Redis 양쪽에서 모두 삭제하여 완전한 로그아웃을 보장합니다.
     * 삭제 작업 실패 시에도 예외를 발생시키지 않고 로그만 기록합니다.
     *
     * 삭제 대상:
     * - "access_token:{kakaoId}"
     * - "refresh_token:{kakaoId}"
     *
     * @param kakaoId 토큰을 삭제할 사용자의 카카오 ID
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
     * 리프레시 토큰의 유효성을 검증합니다
     *
     * 클라이언트가 제공한 리프레시 토큰과 Redis에 저장된 토큰을 비교하여 유효성을 확인합니다.
     * 토큰 갱신 API에서 사용되며, 저장된 토큰과 정확히 일치하는 경우에만 유효한 것으로 판단합니다.
     * Redis에서 토큰을 찾을 수 없거나 값이 다른 경우 false를 반환합니다.
     *
     * @param kakaoId 검증할 사용자의 카카오 ID
     * @param refreshToken 클라이언트가 제공한 리프레시 토큰
     * @return 토큰 유효성 여부 (true: 유효, false: 무효)
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