package Baemin.News_Deliver.Domain.Kakao.service;

import Baemin.News_Deliver.Domain.Mypage.DTO.SettingDTO;
import Baemin.News_Deliver.Domain.Mypage.Repository.SettingRepository;
import Baemin.News_Deliver.Domain.Mypage.service.SettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public List<String> getCron(Long userId) {
        List<SettingDTO> settings = settingService.getAllSettingsByUserId(userId);

        List<String> crons = new ArrayList<>();

        for (SettingDTO setting : settings) {
            // 1. deliveryTime, days 가져오기
            String deliveryTime = setting.getDeliveryTime().toString();
            List<Integer> days = setting.getDays();

            // 2. 로그 출력
            log.info("deliveryTime: {}", deliveryTime);
            log.info("days: {}", days);

            // 3. 크론 표현식 생성
            String cron = toCron(deliveryTime, days);
            log.info("생성된 Cron 표현식: {}", cron);

            crons.add(cron);
        }

        return crons;
    }

    private String toCron(String deliveryTimeStr, List<Integer> days) {
        LocalDateTime deliveryTime = LocalDateTime.parse(deliveryTimeStr);

        int hour = deliveryTime.getHour();
        int minute = deliveryTime.getMinute();

        String cronDays = days.stream()
                .map(DAY_MAP::get)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));

        return String.format("0 %d %d ? * %s", minute, hour, cronDays);
    }


}
