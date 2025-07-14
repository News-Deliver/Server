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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    /**
     * 카카오 ID로 사용자 조회
     */
    @Transactional(readOnly = true)
    public User findByKakaoId(String kakaoId) {
        return userRepository.findByKakaoId(kakaoId).orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 사용자 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean existsByKakaoId(String kakaoId) {
        return userRepository.existsByKakaoId(kakaoId);
    }

    /**
     * 토큰 갱신
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
     * 로그아웃
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
     * 현재 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String kakaoId) {
        User user = findByKakaoId(kakaoId);
        return new UserResponse(user.getId(), user.getKakaoId(), user.getCreatedAt());
    }
}