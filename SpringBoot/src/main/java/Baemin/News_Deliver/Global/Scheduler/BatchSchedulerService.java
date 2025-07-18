package Baemin.News_Deliver.Global.Scheduler;

import Baemin.News_Deliver.Domain.HotTopic.service.HotTopicService;
import Baemin.News_Deliver.Global.News.Batch.service.BatchService;
import Baemin.News_Deliver.Global.News.ElasticSearch.service.NewsEsService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchSchedulerService {

    private final TaskScheduler taskScheduler;
    private final BatchService batchService;
    private final NewsEsService newsEsService;
    private final SchedulerInitializer schedulerInitializer;
    private final HotTopicService hotTopicService;
    private ScheduledFuture<?> batchFuture;

    /**
     * 서버 시작 시 뉴스 배치 스케줄러 등록
     * 매일 자정(00:00)에 실행
     */

    @PostConstruct
    public void scheduleNewsBatch() {
        String cron = "0 25 14 * * *";

        Runnable batchTask = () -> {
            log.info("[BatchScheduler] 자정 배치 시작 - {}", LocalDateTime.now());

            // 1단계: DB 배치
            try {
                LocalDateTime start = LocalDateTime.now();
                log.info("[BatchScheduler] DB 배치 시작");

                batchService.runBatch();

                LocalDateTime end = LocalDateTime.now();
                log.info("[BatchScheduler] DB 배치 완료 (실행 시간: {}초)", Duration.between(start, end).toSeconds());

            } catch (Exception e) {
                log.error("[BatchScheduler] DB 배치 실패: DB 배치 중 예외 발생: {}", e.getMessage(), e);
                //우선은 코드 중단 이후에 로직은 정책에 맞춰서 작성할 것
                return;
            }

            // 엘라스틱 인덱싱
            try {
                LocalDateTime start = LocalDateTime.now();
                log.info("[BatchScheduler] 엘라스틱서치 인덱싱 시작");

                newsEsService.esBulkService();

                LocalDateTime end = LocalDateTime.now();
                log.info("[BatchScheduler] 엘라스틱서치 인덱싱 완료 (실행 시간: {}초)", Duration.between(start, end).toSeconds());

            } catch (Exception e) {
                log.error("[BatchScheduler] 엘라스틱 서치 인덱싱 중 예외 발생: {}", e.getMessage(), e);
                //우선은 코드 중단 이후에 로직은 정책에 맞춰서 작성할 것
                return;
            }

            // 핫토픽
            try {
                LocalDateTime start = LocalDateTime.now();
                log.info("[BatchScheduler] 핫토픽 배치 시작");

                hotTopicService.getAndSaveHotTopic();

                LocalDateTime end = LocalDateTime.now();
                log.info("[BatchScheduler] 핫토픽 배치 완료 (실행 시간: {}초)", Duration.between(start, end).toSeconds());

            } catch (Exception e) {
                log.error("[BatchScheduler] 핫토픽 배치 중 예외 발생: {}", e.getMessage(), e);
                //우선은 코드 중단 이후에 로직은 정책에 맞춰서 작성할 것
                return;
            }

            // 사용자 셋팅 스케줄 등록
            try {
                LocalDateTime start = LocalDateTime.now();
                log.info("[BatchScheduler] 사용자 셋팅 스케줄 등록 시작");

                schedulerInitializer.scheduleAllUserSettings();

                LocalDateTime end = LocalDateTime.now();
                log.info("[BatchScheduler] 사용자 셋팅 스케줄 등록 완료 (실행 시간: {}초)", Duration.between(start, end).toSeconds());

            } catch (Exception e) {
                log.error("[BatchScheduler] 사용자 스케줄러 등록 중 예외 발생: {}", e.getMessage(), e);
            }
        };

        batchFuture = taskScheduler.schedule(batchTask, new CronTrigger(cron));
    }


    //원본 코드
//    @PostConstruct
//    public void scheduleNewsBatch() {
//        String cron = "0 0 0 * * *"; // 매일 00:00
//
//        Runnable batchTask = () -> {
//            log.info("[BatchScheduler] 자정 배치 시작 - {}", LocalDateTime.now());
//
//            // 1단계: DB 배치
//            try {
//                LocalDateTime start = LocalDateTime.now();
//                log.info("[BatchScheduler] DB 배치 시작");
//
//                batchService.runBatch();
//
//                LocalDateTime end = LocalDateTime.now();
//                log.info("[BatchScheduler] DB 배치 완료 (실행 시간: {}초)", Duration.between(start, end).toSeconds());
//
//            } catch (Exception e) {
//                log.error("[BatchScheduler] DB 배치 실패: DB 배치 중 예외 발생: {}", e.getMessage(), e);
//                //우선은 코드 중단 이후에 로직은 정책에 맞춰서 작성할 것
//                return;
//            }
//
//            // 엘라스틱 인덱싱
//            try {
//                LocalDateTime start = LocalDateTime.now();
//                log.info("[BatchScheduler] 엘라스틱서치 인덱싱 시작");
//
//                newsEsService.esBulkService();
//
//                LocalDateTime end = LocalDateTime.now();
//                log.info("[BatchScheduler] 엘라스틱서치 인덱싱 완료 (실행 시간: {}초)", Duration.between(start, end).toSeconds());
//
//            } catch (Exception e) {
//                log.error("[BatchScheduler] 엘라스틱 서치 인덱싱 중 예외 발생: {}", e.getMessage(), e);
//                //우선은 코드 중단 이후에 로직은 정책에 맞춰서 작성할 것
//                return;
//            }
//
//            // 사용자 셋팅 스케줄 등록
//            try {
//                LocalDateTime start = LocalDateTime.now();
//                log.info("[BatchScheduler] 사용자 셋팅 스케줄 등록 시작");
//
//                schedulerInitializer.scheduleAllUserSettings();
//
//                LocalDateTime end = LocalDateTime.now();
//                log.info("[BatchScheduler] 사용자 셋팅 스케줄 등록 완료 (실행 시간: {}초)", Duration.between(start, end).toSeconds());
//
//            } catch (Exception e) {
//                log.error("[BatchScheduler] 사용자 스케줄러 등록 중 예외 발생: {}", e.getMessage(), e);
//            }
//        };
//
//        batchFuture = taskScheduler.schedule(batchTask, new CronTrigger(cron));
//    }

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
