package Baemin.News_Deliver.Global.Scheduler;

import Baemin.News_Deliver.Global.News.JPAINSERT.service.BatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {

    private final BatchService batchService;

    //@Scheduled(cron = "0 0 0 * * *") // 매일 자정마다 로드
    @Scheduled(cron = "* */15 * * * *") //[테스트용] 스케쥴러 동작 확인용 3초마다 테스트
    public void dataBatchRun() {
        log.info("[Scheduler] database 배치 실행됨 - 시간: {}", LocalDateTime.now());

        //배치 코드 넘기기
        batchService.batch();
    }

}
