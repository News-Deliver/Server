package Baemin.News_Deliver.Domain.Auth.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 정보를 저장하는 엔티티 클래스
 *
 * 카카오 OAuth2 로그인을 통해 가입한 사용자의 기본 정보를 관리합니다.
 * 카카오에서 제공하는 고유 ID를 통해 사용자를 식별하며,
 * 뉴스 배달 서비스의 모든 기능은 이 사용자 정보를 기반으로 동작합니다.
 */
@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    @Column(name = "user_id", nullable = false, unique = true)
    private String kakaoId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 사용자 엔티티를 생성합니다
     *
     * 카카오 OAuth2 로그인 성공 시 새로운 사용자를 생성할 때 사용됩니다.
     * 생성 시점은 JPA Auditing에 의해 자동으로 설정됩니다.
     *
     * @param kakaoId 카카오에서 제공하는 사용자 고유 ID
     */
    @Builder
    public User(String kakaoId) {
        this.kakaoId = kakaoId;
    }
}