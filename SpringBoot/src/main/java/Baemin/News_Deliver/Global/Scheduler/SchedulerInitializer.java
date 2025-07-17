package Baemin.News_Deliver.Global.Scheduler;

import Baemin.News_Deliver.Domain.Kakao.service.KakaoSchedulerService;
import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import Baemin.News_Deliver.Domain.Mypage.service.SettingService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerInitializer { //서버시작시 또는 특정 시간에 모든 셋팅값을 가져옴.
    private final SettingService settingService;
    private final TaskSchedulerService taskSchedulerService;
    private final KakaoSchedulerService kakaoSchedulerService;

    /**
     * 서버 시작 시 전체 스케줄 초기화(테스트용)
     */
    @PostConstruct
    public void init() {
        scheduleAllUserSettings();
        //scheduleNewsBatch();
    }


    @PostConstruct
    //@Scheduled(cron = "0 0 5 * * *") // 매일 새벽 5시 > 추후에 배치가 끝나면 자동으로 할 수 있도록 정책 개선 예정
    public void scheduleAllUserSettings() {
        List<Setting> settings = settingService.getAllSettings();

        for (Setting setting : settings) {
            Long userId = setting.getUser().getId();

            List<String> cronList = kakaoSchedulerService.getCron(userId); //여러개의 설정값을 리스트로 받아옴
            for (String cron : cronList) {
                log.info("[SchedulerInit] 유저 {}에 대해 cron 등록: {}", userId, cron);
                taskSchedulerService.scheduleUser(userId, cron);
            }
        }
    }
}
