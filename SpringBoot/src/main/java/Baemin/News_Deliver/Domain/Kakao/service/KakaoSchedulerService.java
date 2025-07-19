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

    /**
     * 스케쥴러로 부터 Setting 엔티티를 받아, 크론 반환하는 메서드
     */
    public String getCron(Setting setting) {
        LocalDateTime deliveryTime = setting.getDeliveryTime();
        List<Days> days = setting.getDays();

        log.info("deliveryTime: {}", deliveryTime);
        log.info("days: {}", days);

        String cron = toCron(deliveryTime, days);
        log.info("생성된 Cron 표현식: {}", cron);
        return cron;
    }

    /**
     * 크론 표현식으로 변환하는 메서드
     */
    private String toCron(LocalDateTime deliveryTime, List<Days> days) {
        int hour = deliveryTime.getHour();
        int minute = deliveryTime.getMinute();

        String cronDays = days.stream()
                .map(day -> DAY_MAP.get(day.getDeliveryDay()))
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));

        return String.format("0 %d %d ? * %s", minute, hour, cronDays);
    }


}
