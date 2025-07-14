package Baemin.News_Deliver.Global.JWT;

import Baemin.News_Deliver.Global.Config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final SecretKey jwtSecretKey;
    private final JwtConfig jwtConfig;

    /**
     * Access Token 생성
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
     * Refresh Token 생성
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
     * 토큰에서 카카오 ID 추출
     */
    public String getKakaoIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    /**
     * 토큰 유효성 검증
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
     * 토큰 만료 여부 확인
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
     * 토큰 파싱
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}