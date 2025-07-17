package Baemin.News_Deliver.Domain.Auth.Service;

import Baemin.News_Deliver.Domain.Auth.Entity.User;
import Baemin.News_Deliver.Domain.Auth.Exception.AuthException;
import Baemin.News_Deliver.Domain.Auth.Repository.UserRepository;
import Baemin.News_Deliver.Domain.Auth.dto.TokenResponse;
import Baemin.News_Deliver.Domain.Auth.dto.UserResponse;
import Baemin.News_Deliver.Global.Exception.ErrorCode;
import Baemin.News_Deliver.Global.JWT.JwtTokenProvider;
import Baemin.News_Deliver.Global.Redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 인증 및 JWT 토큰 관리를 담당하는 서비스
 *
 * 카카오 OAuth2 로그인을 통한 사용자 인증, JWT 토큰 발급/갱신/삭제,
 * 사용자 정보 조회 등의 핵심 인증 비즈니스 로직을 처리합니다.
 * Redis를 통한 토큰 저장소 관리와 사용자 정보 데이터베이스 연동을 담당합니다.
 *
 * @see JwtTokenProvider
 * @see RedisService
 * @see UserRepository
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    /**
     * 카카오 ID로 사용자를 조회합니다
     *
     * 데이터베이스에서 주어진 카카오 ID에 해당하는 사용자를 찾습니다.
     * 사용자가 존재하지 않을 경우 AuthException을 발생시킵니다.
     *
     * @param kakaoId 카카오에서 제공하는 사용자 고유 ID
     * @return 조회된 사용자 엔티티
     * @throws AuthException 사용자를 찾을 수 없는 경우 (ErrorCode.USER_NOT_FOUND)
     */
    @Transactional(readOnly = true)
    public User findByKakaoId(String kakaoId) {
        return userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 카카오 ID로 사용자 존재 여부를 확인합니다
     *
     * 데이터베이스에 주어진 카카오 ID를 가진 사용자가 존재하는지 확인합니다.
     * 예외를 발생시키지 않고 boolean 값으로 결과를 반환합니다.
     *
     * @param kakaoId 카카오에서 제공하는 사용자 고유 ID
     * @return 사용자 존재 여부 (true: 존재, false: 미존재)
     */
    @Transactional(readOnly = true)
    public boolean existsByKakaoId(String kakaoId) {
        return userRepository.existsByKakaoId(kakaoId);
    }

    /**
     * JWT 토큰을 갱신합니다
     *
     * 클라이언트로부터 받은 리프레시 토큰을 검증하고,
     * 유효한 경우 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.
     * 새로 발급된 토큰들은 Redis에 저장되어 관리됩니다.
     *
     * @param refreshToken 클라이언트가 제공한 JWT 리프레시 토큰
     * @return 새로 발급된 액세스 토큰과 리프레시 토큰을 포함한 응답 DTO
     * @throws AuthException 리프레시 토큰이 유효하지 않은 경우 (ErrorCode.REFRESH_TOKEN_INVALID)
     * @throws AuthException 사용자를 찾을 수 없는 경우 (ErrorCode.USER_NOT_FOUND)
     * @throws AuthException 토큰 저장 중 오류가 발생한 경우 (ErrorCode.TOKEN_STORAGE_FAILED)
     */
    public TokenResponse refreshToken(String refreshToken) {
        try {
            // 1. Refresh Token 유효성 검증
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new AuthException(ErrorCode.REFRESH_TOKEN_INVALID);
            }

            // 2. Refresh Token에서 kakaoId 추출
            String kakaoId = jwtTokenProvider.getKakaoIdFromToken(refreshToken);

            // 3. Redis에서 저장된 Refresh Token과 비교 검증
            if (!redisService.validateRefreshToken(kakaoId, refreshToken)) {
                throw new AuthException(ErrorCode.REFRESH_TOKEN_INVALID);
            }

            // 4. 사용자 존재 확인
            if (!existsByKakaoId(kakaoId)) {
                throw new AuthException(ErrorCode.USER_NOT_FOUND);
            }

            // 5. 새로운 토큰들 생성
            String newAccessToken = jwtTokenProvider.generateAccessToken(kakaoId);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(kakaoId);

            // 6. Redis에 새 토큰 저장
            redisService.saveAccessToken(kakaoId, newAccessToken);
            redisService.saveRefreshToken(kakaoId, newRefreshToken);

            log.info("토큰 갱신 성공: kakaoId = {}", kakaoId);
            return new TokenResponse(newAccessToken, newRefreshToken, "Bearer", 1800);

        } catch (AuthException e) {
            log.error("토큰 갱신 실패: kakaoId = {}, errorCode = {}",
                    jwtTokenProvider.getKakaoIdFromToken(refreshToken), e.getErrorcode().getErrorCode());
            throw e; // AuthException은 그대로 재던지기
        } catch (Exception e) {
            log.error("토큰 갱신 중 예상치 못한 오류: {}", e.getMessage());
            throw new AuthException(ErrorCode.TOKEN_STORAGE_FAILED);
        }
    }

    /**
     * 사용자를 로그아웃 처리합니다
     *
     * Redis에 저장된 사용자의 액세스 토큰과 리프레시 토큰을 모두 삭제하여
     * 토큰을 무효화시킵니다. 클라이언트는 새로 로그인해야 API를 사용할 수 있습니다.
     *
     * @param kakaoId 로그아웃할 사용자의 카카오 ID
     * @throws AuthException 토큰 삭제 중 오류가 발생한 경우 (ErrorCode.TOKEN_STORAGE_FAILED)
     */
    public void logout(String kakaoId) {
        try {
            // Redis에서 토큰 삭제
            redisService.deleteTokens(kakaoId);
            log.info("로그아웃 성공: kakaoId = {}", kakaoId);
        } catch (Exception e) {
            log.error("로그아웃 실패: kakaoId = {}, error = {}", kakaoId, e.getMessage());
            throw new AuthException(ErrorCode.TOKEN_STORAGE_FAILED);
        }
    }

    /**
     * 현재 사용자의 기본 정보를 조회합니다
     *
     * 인증된 사용자의 카카오 ID를 기반으로 사용자 정보를 조회하고,
     * 클라이언트에게 전달하기 위한 응답 DTO로 변환하여 반환합니다.
     *
     * @param kakaoId 정보를 조회할 사용자의 카카오 ID
     * @return 사용자의 기본 정보를 포함한 응답 DTO
     * @throws AuthException 사용자를 찾을 수 없는 경우 (ErrorCode.USER_NOT_FOUND)
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String kakaoId) {
        User user = findByKakaoId(kakaoId); // 이미 AuthException 처리됨
        return new UserResponse(user.getId(), user.getKakaoId(), user.getCreatedAt());
    }
}