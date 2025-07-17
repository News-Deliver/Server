package Baemin.News_Deliver.Global.Scheduler;

import Baemin.News_Deliver.Global.News.Batch.service.BatchService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchSchedulerService {

    private final TaskScheduler taskScheduler;
    private final BatchService batchService;

    private ScheduledFuture<?> batchFuture;

    /**
     * 서버 시작 시 뉴스 배치 스케줄러 등록
     * 매일 자정(00:00)에 실행
     */
    @PostConstruct
    public void scheduleNewsBatch() {
        String cron = "0 0 0 * * *"; // 매일 00:00

        Runnable batchTask = () -> {
            log.info("[BatchScheduler] 자정 배치 시작 - {}", LocalDateTime.now());

            try {
                batchService.runBatch();
                log.info("[BatchScheduler] 자정 배치 정상 완료");

            } catch (Exception e) {
                log.error("[BatchScheduler] 자정 배치 실패: {}", e.getMessage(), e);

                // 필요 시 재시도 로직 넣거나, 정책에 맞춰서 넣어둬야함.
            }
        };

        batchFuture = taskScheduler.schedule(batchTask, new CronTrigger(cron));
    }

    /**
     * 수동으로 배치 스케줄 취소 (필요한 경우)
     */
    public void cancelNewsSchedule() {
        if (batchFuture != null && !batchFuture.isCancelled()) {
            batchFuture.cancel(false);
            log.info("[BatchScheduler] 자정 뉴스 배치 스케줄 취소");
        }
    }
}
