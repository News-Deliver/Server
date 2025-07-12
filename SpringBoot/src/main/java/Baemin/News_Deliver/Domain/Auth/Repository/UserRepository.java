package Baemin.News_Deliver.Domain.Auth.Repository;

import Baemin.News_Deliver.Domain.Auth.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 카카오 ID로 사용자 조회
    Optional<User> findByKakaoId(String kakaoId);

    // 카카오 ID로 사용자 존재 여부 확인
    boolean existsByKakaoId(String kakaoId);
}