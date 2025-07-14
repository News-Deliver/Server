package Baemin.News_Deliver.Domain.Auth.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 인증 정보를 관리하는 엔티티
 *
 * 카카오 OAuth2 로그인을 통해 발급받은 리프레시 토큰을 저장하여
 * 카카오 API 호출 시 필요한 액세스 토큰을 재발급받을 수 있도록 합니다.
 * User 엔티티와 일대일 관계를 가지며, 각 사용자당 하나의 Auth 정보를 보유합니다.
 *
 * @see User
 */
@Entity
@Table(name = "auth")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "kakao_refresh_key", columnDefinition = "VARCHAR(255)")
    private String kakaoRefreshToken;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_key", nullable = false)
    private User user;


    /**
     * 인증 정보 엔티티 생성자
     *
     * 카카오 로그인 성공 시 새로운 인증 정보를 생성할 때 사용됩니다.
     * 사용자와 카카오 리프레시 토큰을 함께 저장합니다.
     *
     * @param kakaoRefreshToken 카카오에서 발급받은 리프레시 토큰
     * @param user 연관된 사용자 엔티티
     */
    @Builder
    public Auth(String kakaoRefreshToken, User user) {
        this.kakaoRefreshToken = kakaoRefreshToken;
        this.user = user;
    }

    /**
     * 카카오 리프레시 토큰을 업데이트합니다
     *
     * 카카오 로그인을 다시 수행하거나 리프레시 토큰이 갱신되었을 때
     * 기존 인증 정보를 새로운 토큰으로 업데이트하는 데 사용됩니다.
     *
     * @param kakaoRefreshToken 새로운 카카오 리프레시 토큰
     */
    public void updateKakaoRefreshToken(String kakaoRefreshToken) {
        this.kakaoRefreshToken = kakaoRefreshToken;
    }
}