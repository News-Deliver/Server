package Baemin.News_Deliver.Global.JWT;

import Baemin.News_Deliver.Global.Config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 토큰 생성, 검증, 파싱을 담당하는 유틸리티 클래스
 *
 * 사용자 인증을 위한 JWT 액세스 토큰과 리프레시 토큰의 생성 및 검증을 처리합니다.
 * HMAC SHA-256 알고리즘을 사용하여 토큰에 서명하며, 토큰의 만료 시간과 유효성을 관리합니다.
 *
 * 주요 기능:
 * - 액세스 토큰 생성 (유효기간: 30분)
 * - 리프레시 토큰 생성 (유효기간: 24시간)
 * - 토큰 유효성 검증
 * - 토큰에서 사용자 정보 추출
 *
 * @see JwtConfig
 * @see JwtAuthenticationFilter
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final SecretKey jwtSecretKey;
    private final JwtConfig jwtConfig;

    /**
     * JWT 액세스 토큰을 생성합니다
     *
     * 사용자의 카카오 ID를 subject로 하는 액세스 토큰을 생성합니다.
     * 토큰에는 사용자 식별 정보와 토큰 타입이 포함되며, 30분의 유효기간을 가집니다.
     * API 요청 시 Authorization 헤더에 포함되어 사용자 인증에 사용됩니다.
     *
     * @param kakaoId 토큰에 포함될 사용자의 카카오 ID
     * @return HMAC SHA-256으로 서명된 JWT 액세스 토큰 문자열
     */
    public String generateAccessToken(String kakaoId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getAccessTokenExpiration());

        return Jwts.builder()
                .setSubject(kakaoId)
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT 리프레시 토큰을 생성합니다
     *
     * 액세스 토큰 만료 시 새로운 토큰을 발급받기 위한 리프레시 토큰을 생성합니다.
     * 24시간의 유효기간을 가지며, Redis에 저장되어 토큰 갱신 시 검증에 사용됩니다.
     *
     * @param kakaoId 토큰에 포함될 사용자의 카카오 ID
     * @return HMAC SHA-256으로 서명된 JWT 리프레시 토큰 문자열
     */
    public String generateRefreshToken(String kakaoId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getRefreshTokenExpiration());

        return Jwts.builder()
                .setSubject(kakaoId)
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT 토큰에서 카카오 ID를 추출합니다
     *
     * 주어진 JWT 토큰을 파싱하여 subject 클레임에서 사용자의 카카오 ID를 추출합니다.
     * 토큰의 유효성 검증은 수행하지 않으므로, 사전에 validateToken()으로 검증해야 합니다.
     *
     * @param token 카카오 ID를 추출할 JWT 토큰 문자열
     * @return 토큰에 포함된 사용자의 카카오 ID
     * @throws JwtException 토큰 파싱에 실패한 경우
     */
    public String getKakaoIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    /**
     * JWT 토큰의 유효성을 검증합니다
     *
     * 토큰의 서명, 만료 시간, 형식 등을 종합적으로 검증합니다.
     * 검증에 실패한 경우 구체적인 실패 사유를 로그로 기록하고 false를 반환합니다.
     *
     * 검증 항목:
     * - 토큰 서명 유효성 (HMAC SHA-256)
     * - 토큰 만료 시간
     * - 토큰 형식 (JWT 표준 준수)
     * - 클레임 존재 여부
     *
     * @param token 검증할 JWT 토큰 문자열
     * @return 토큰 유효성 여부 (true: 유효, false: 무효)
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (SignatureException e) {
            log.error("잘못된 JWT 서명: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("잘못된 형식의 JWT 토큰: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims가 비어있음: {}", e.getMessage());
        }
        return false;
    }

    /**
     * JWT 토큰의 만료 여부를 확인합니다
     *
     * 토큰에서 만료 시간을 추출하여 현재 시간과 비교합니다.
     * 토큰 파싱에 실패하거나 예외가 발생한 경우 만료된 것으로 간주합니다.
     *
     * @param token 만료 여부를 확인할 JWT 토큰 문자열
     * @return 토큰 만료 여부 (true: 만료됨, false: 유효함)
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = parseClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * JWT 토큰을 파싱하여 클레임을 추출합니다
     *
     * 내부적으로 사용되는 메서드로, JWT 토큰을 파싱하여 클레임 객체를 반환합니다.
     * 시크릿 키를 사용하여 토큰의 서명을 검증하며, 파싱에 실패한 경우 예외를 발생시킵니다.
     *
     * @param token 파싱할 JWT 토큰 문자열
     * @return 토큰에서 추출된 클레임 객체
     * @throws JwtException 토큰 파싱 또는 서명 검증에 실패한 경우
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}