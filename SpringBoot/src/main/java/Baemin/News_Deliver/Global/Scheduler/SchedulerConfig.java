package Baemin.News_Deliver.Global.Scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * {@code SchedulerConfig}는 간단한 테스트용 스케줄러 클래스입니다.
 * <p>
 * Spring의 {@link org.springframework.scheduling.annotation.Scheduled} 어노테이션을 통해
 * 주기적으로 작업을 실행할 수 있으며, 현재는 테스트 용도로만 정의되어 있습니다.
 * </p>
 *
 * <p>
 * 향후 실제 배치 서비스 호출이나 데이터 초기화 작업 등으로 확장할 수 있습니다.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {

    //private final JpaService batchService;

    //@Scheduled(cron = "0 0 0 * * *") // 매일 자정마다 로드
    //@Scheduled(cron = "* * * * * *") //[테스트용] 스케쥴러 동작 확인용 3초마다 테스트
    public void dataBatchRun() {
        log.info("[Scheduler] database 배치 실행됨 - 시간: {}", LocalDateTime.now());

        //배치 코드 넘기기
        //batchService.batch();
    }

}
