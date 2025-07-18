package Baemin.News_Deliver.Domain.Kakao.service;

import Baemin.News_Deliver.Domain.Mypage.DTO.SettingDTO;
import Baemin.News_Deliver.Domain.Mypage.Entity.Days;
import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import Baemin.News_Deliver.Domain.Mypage.Repository.SettingRepository;
import Baemin.News_Deliver.Domain.Mypage.service.SettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoSchedulerService {

    private final SettingService settingService;

    //userId 파라미터로 받아, Cron반환해주는 코드
    public ResponseEntity getcron(Long userId) {
        return ResponseEntity.ok(getCron(userId));
    }

    //1(월)~7(일)로 매핑
    private static final Map<Integer, String> DAY_MAP = Map.of(
            1, "MON",
            2, "TUE",
            3, "WED",
            4, "THU",
            5, "FRI",
            6, "SAT",
            7, "SUN"
    );

    public String getCron(Long settingId) {
        // 단일 Setting 엔티티 가져오기
        Setting setting = settingService.getById(settingId);

        LocalDateTime deliveryTime = setting.getDeliveryTime(); // 문자열 변환 없이 그대로 사용
        List<Days> days = setting.getDays(); // Days enum

        // 로그 출력
        log.info("deliveryTime: {}", deliveryTime);
        log.info("days: {}", days);

        // 크론 생성
        String cron = toCron(deliveryTime, days);
        log.info("생성된 Cron 표현식: {}", cron);

        return cron;
    }

    private String toCron(LocalDateTime deliveryTime, List<Days> days) {
        int hour = deliveryTime.getHour();
        int minute = deliveryTime.getMinute();

        String cronDays = days.stream()
                .map(day -> DAY_MAP.get(day.getDeliveryDay()))  // day.getDay()가 int (1~7) 리턴
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));

        return String.format("0 %d %d ? * %s", minute, hour, cronDays);
    }


}
