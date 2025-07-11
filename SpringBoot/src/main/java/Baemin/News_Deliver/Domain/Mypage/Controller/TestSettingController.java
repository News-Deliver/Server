package Baemin.News_Deliver.Domain.Mypage.Controller;

import Baemin.News_Deliver.Domain.Auth.Entity.User;
import Baemin.News_Deliver.Domain.Auth.Repository.UserRepository;
import Baemin.News_Deliver.Domain.Mypage.Entity.Days;
import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import Baemin.News_Deliver.Domain.Mypage.Entity.SettingBlockKeyword;
import Baemin.News_Deliver.Domain.Mypage.Entity.SettingKeyword;
import Baemin.News_Deliver.Domain.Mypage.Repository.DaysRepository;
import Baemin.News_Deliver.Domain.Mypage.Repository.SettingBlockKeywordRepository;
import Baemin.News_Deliver.Domain.Mypage.Repository.SettingKeywordRepository;
import Baemin.News_Deliver.Domain.Mypage.Repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestSettingController {

    private final SettingRepository settingRepository;
    private final UserRepository userRepository;
    private final DaysRepository daysRepository;
    private final SettingKeywordRepository settingKeywordRepository;
    private final SettingBlockKeywordRepository settingBlockKeywordRepository;  // 현재 미작동

    // Setting 생성 테스트
    @PostMapping("/setting")
    public String createSetting(@RequestParam String kakaoId) {
        // 1. User 조회 (kakaoId로 찾기)
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + kakaoId));

        // 2. Setting 생성
        Setting setting = Setting.builder()
                .deliveryTime(LocalTime.of(14, 30))
                .startDate(LocalDateTime.now())
                .endDate(null)
                .isDeleted(false)
                .user(user)
                .build();

        // 3. 저장
        Setting saved = settingRepository.save(setting);

        return "Setting 생성 완료! ID: " + saved.getId() + ", User: " + user.getKakaoId();
    }

    // Setting 조회 테스트 - Days + Keywords + BlockKeywords(금요일 오전 기준 미포함) 포함
    @GetMapping("/setting")
    public String getSettings(@RequestParam String kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + kakaoId));

        List<Setting> settings = settingRepository.findByUser(user);

        StringBuilder result = new StringBuilder();
        result.append("찾은 설정 개수: ").append(settings.size()).append("\n");

        for (Setting setting : settings) {
            result.append("ID: ").append(setting.getId())
                    .append(", 시간: ").append(setting.getDeliveryTime())
                    .append(", 시작일: ").append(setting.getStartDate())
                    .append(", Days 개수: ").append(setting.getDays().size())
                    .append(", Keywords 개수: ").append(setting.getKeywords().size())
                    .append(", Block Keywords 개수: ").append(setting.getBlockKeywords().size())  // 추가!
                    .append("\n");

            // Days 목록 표시
            for (Days day : setting.getDays()) {
                result.append("  - 요일: ").append(day.getDeliveryDays()).append("\n");
            }

            // Keywords 목록 표시
            for (SettingKeyword keyword : setting.getKeywords()) {
                result.append("  - 키워드: ").append(keyword.getSettingKeyword()).append("\n");
            }

            // Block Keywords 목록 표시 (새로 추가!)
            for (SettingBlockKeyword blockKeyword : setting.getBlockKeywords()) {
                result.append("  - 제외 키워드: ").append(blockKeyword.getSettingKeyword()).append("\n");
            }
        }

        return result.toString();
    }

    // User 테이블 확인용 추가
    @GetMapping("/users")
    public String getAllUsers() {
        List<User> users = userRepository.findAll();
        StringBuilder result = new StringBuilder();
        result.append("사용자 수: ").append(users.size()).append("\n");

        for (User user : users) {
            result.append("ID: ").append(user.getId())
                    .append(", KakaoID: ").append(user.getKakaoId())
                    .append(", 생성일: ").append(user.getCreatedAt())
                    .append("\n");
        }

        return result.toString();
    }

    // Days 생성 테스트
    @PostMapping("/days")
    public String createDays(@RequestParam Long settingId, @RequestParam String day) {
        // 1. Setting 조회
        Setting setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new RuntimeException("설정을 찾을 수 없습니다"));

        // 2. Days 생성
        Days days = Days.builder()
                .deliveryDays(day)
                .setting(setting)
                .build();

        // 3. 저장
        Days saved = daysRepository.save(days);

        return "Days 생성 완료! ID: " + saved.getId() + ", 요일: " + saved.getDeliveryDays();
    }

    // SettingKeyword 생성 테스트
    @PostMapping("/keyword")
    public String createKeyword(@RequestParam Long settingId, @RequestParam String keyword) {
        // 1. Setting 조회
        Setting setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new RuntimeException("설정을 찾을 수 없습니다"));

        // 2. SettingKeyword 생성
        SettingKeyword settingKeyword = SettingKeyword.builder()
                .settingKeyword(keyword)
                .setting(setting)
                .build();

        // 3. 저장
        SettingKeyword saved = settingKeywordRepository.save(settingKeyword);

        return "Keyword 생성 완료! ID: " + saved.getId() + ", 키워드: " + saved.getSettingKeyword();
    }

    // SettingBlockKeyword 생성 테스트
    // 블랙키워드 작성중-    '-"  작성시 헤더 오류
    @PostMapping("/blockKeyword")
    public String createBlockKeyword(@RequestParam Long settingId, @RequestParam String keyword) {
        // 1. Setting 조회
        Setting setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new RuntimeException("설정을 찾을 수 없습니다"));

        // 2. SettingBlockKeyword 생성
        SettingBlockKeyword blockKeyword = SettingBlockKeyword.builder()
                .settingKeyword(keyword)
                .setting(setting)
                .build();

        // 3. 저장
        SettingBlockKeyword saved = settingBlockKeywordRepository.save(blockKeyword);

        return "Block Keyword 생성 완료! ID: " + saved.getId() + ", 제외 키워드: " + saved.getSettingKeyword();
    }
}