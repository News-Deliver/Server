package Baemin.News_Deliver.Domain.Auth.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auth")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_id")
    private Long id;

    @Column(name = "kakao_refresh_token", columnDefinition = "TEXT")
    private String kakaoRefreshToken;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Auth(String kakaoRefreshToken, User user) {
        this.kakaoRefreshToken = kakaoRefreshToken;
        this.user = user;
    }

    // 카카오 리프레시 토큰 업데이트
    public void updateKakaoRefreshToken(String kakaoRefreshToken) {
        this.kakaoRefreshToken = kakaoRefreshToken;
    }
}