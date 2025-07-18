package Baemin.News_Deliver.Global.Scheduler;

import Baemin.News_Deliver.Domain.Kakao.service.KakaoSchedulerService;
import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import Baemin.News_Deliver.Domain.Mypage.Repository.SettingRepository;
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
    private final SettingRepository settingRepository;
    //private final BatchSchedulerService batchSchedulerService;

    /**
     * 서버 시작 시 전체 스케줄 초기화(테스트용)
     */
    @PostConstruct
    public void init() {

        //batchSchedulerService.scheduleNewsBatch();

        //DB 없을 시 대체 오류 확인
//        if (isSettingTableAvailable()) {
//            scheduleAllUserSettings();
//        } else {
//            log.warn("[SchedulerInit] setting 테이블이 없어 스케줄러를 실행하지 않습니다.");
//        }

    }


    //@PostConstruct
    //@Scheduled(cron = "0 0 5 * * *") // 매일 새벽 5시 > 추후에 배치가 끝나면 자동으로 할 수 있도록 정책 개선 예정
    public void scheduleAllUserSettings() {
        List<Setting> settings = settingService.getAllSettings();

        //DB에 settings값이 없을 때 스케쥴러 취소 코드
        if (settings == null || settings.isEmpty()) {
            log.warn("[SchedulerInit] 등록할 Setting이 없어 스케줄러를 실행하지 않습니다.");
            return;
        }

        for (Setting setting : settings) {
            Long userId = setting.getUser().getId();

            List<String> cronList = kakaoSchedulerService.getCron(userId); //여러개의 설정값을 리스트로 받아옴
            for (String cron : cronList) {
                log.info("[SchedulerInit] 유저 {}에 대해 cron 등록: {}", userId, cron);
                taskSchedulerService.scheduleUser(userId, cron);
            }
        }
    }

    //테이블 존재 확인용 코드
    private boolean isSettingTableAvailable() {
        try {
            settingRepository.count();
            return true;
        } catch (Exception e) {
            log.warn("[SchedulerInit] 테이블 확인 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

}
