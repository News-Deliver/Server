package Baemin.News_Deliver.Global.NewsMonitoring.Service;


import Baemin.News_Deliver.Global.NewsMonitoring.Manager.NewsMonitoringManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsMonitoringService {

    private static final String[] sections = {"politics", "economy", "society", "culture", "tech", "entertainment", "opinion"};

    private final IntermediateBatchRedisService intermediateBatchRedisService;
    private final NewsMonitoringManager newsMonitoringManager;
    private final JobLauncher jobLauncher;
    private final Job newsDataSaveJob_Monitoring;

    @Scheduled(cron = "0 0 * * * *") // 매 시간 정각
    //@Scheduled(cron = "0 */5 * * * *") // 5분
    //@Scheduled(cron = "0 */3 * * * *") // 3분
    //@Scheduled(cron = "0 */2 * * * *") // 2분
    //@Scheduled(cron = "0 * * * * *") // 1분
    public void monitoring() {

        /* 날짜 & 시간 확인 */
        LocalDate day = LocalDate.now(); // 오늘의 날짜
        LocalDateTime time = LocalDateTime.now(); // 현재 날짜와 시간(분 단위 포함)
        log.info("// ======================= 오늘의 날짜 : {} =========================", day);
        log.info("현재 시간 : {}", time);

        // 자정(00시)이면 스킵
        if (time.getHour() == 0) {
            log.info("[SKIP] 자정 00시에는 모니터링 작업을 실행하지 않습니다.");
            return;
        }

        /* 날짜 포매팅 작업 */
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateStr = day.format(formatter);

        /* 섹션 별 누적 데이터 수 모니터링 로직 */
        for (String section : sections) {

            int total_items = newsMonitoringManager.getTotalItems(section, dateStr, dateStr);
            String batchDoneKey = "batch_done:" + dateStr + ":" + section;              // 하루 1회 체크용
            String batchCountKey = "batch_count:" + dateStr + ":" + section;           // 하루 횟수 제한용

            /* =================== 9000 이상 18000 이하일 경우 =================== */
            if (total_items >= 9000 && total_items <= 18000) {

                // 하루 한 번만 수행: 이미 했는지 Redis에 확인
                if (intermediateBatchRedisService.isBatchAlreadyDone(batchDoneKey)) {
                    log.info("[SKIP] {} 섹션은 이미 {}에 중간 배치가 수행됨", section, dateStr);
                    continue;
                }

                int prevCount = intermediateBatchRedisService.getBatchCount(batchCountKey);
                log.info("{} 섹션 기존 중간 배치 현황 : 총 {}회 중간 배치", section, prevCount);

                runNewsBatch(section); // 배치 실행

                intermediateBatchRedisService.incrementBatchCount(batchCountKey); // 횟수 증가
                intermediateBatchRedisService.markBatchDone(batchDoneKey);        // 하루 1회 완료 기록

            }

            /* =================== 18000 초과일 경우 =================== */
            else if (total_items > 18000) {

                int count = intermediateBatchRedisService.getBatchCount(batchCountKey);

                if (count >= 3) {
                    log.warn("[SKIP] {} 섹션은 이미 {}에 3회 이상 배치됨", section, dateStr);
                    continue;
                }

                runNewsBatch(section); // 배치 실행

                intermediateBatchRedisService.incrementBatchCount(batchCountKey); // 횟수 증가
                log.warn("[#주의#] {} 섹션의 total_items 수 : {} (누적 {}회 배치됨)", section, total_items, count + 1);
            }
        }
    }

    // ======================= deprecated =========================

    /**
     * 매 정각마다 실행 되는 모니터링 스케줄러
     *
     * 각 섹션 별 현재 몇개의 데이터가 모였는지 모니터링
     * 9000개가 넘는 섹션 발견 시, 즉시 DB에 Batch 실시
     *
     */
//    @Scheduled(cron = "0 0 * * * *") // 매 시간 정각
//    //@Scheduled(cron = "0 */5 * * * *") // 5분
//    // @Scheduled(cron = "0 */3 * * * *") // 3분
//    //@Scheduled(cron = "0 */2 * * * *") // 2분
//    //@Scheduled(cron = "0 * * * * *") // 1분
//    public void monitoring(){
//
//        /* 날짜 & 시간 확인 */
//        LocalDate day = LocalDate.now(); // 오늘의 날짜
//        LocalDateTime time = LocalDateTime.now(); // 현재 날짜와 시간(분 단위 포함)
//        log.info("// ======================= 오늘의 날짜 : {} =========================", day); // 오늘의 날짜 로그 확인
//        log.info("현재 시간 : {}", time);
//
//        /* 날짜 포매팅 작업 */
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); //원하는 포맷 지정
//        String dateTo = day.format(formatter); //문자열로 변환
//        String dateFrom = day.format(formatter); //문자열로 변환
//
//        /* 섹션 별 누적 데이터 수 모니터링 로직 */
//        for (String section : sections) {
//
//            // 현재 섹션의 현 시각 누적 Total_Item 수 확인
//            int total_items = newsMonitoringManager.getTotalItems(section, dateFrom, dateTo);
//
//            /* 뉴스 데이터 9000개 이상 시 중간 배치 + Redis 기록 */
//            if(total_items >= 9000 && total_items <= 18000){
//
//                // 해당 섹션이 이전에도 사전 Batch 작업이 실행 되었는지 확인 (Redis에서 횟수 조회)
//                int n = intermediateBatchRedisService.getBatchCount(section);
//                log.info("{} 섹션 기존 중간 배치 현황 : 총 {}회 중간 배치 ",section, n);
//
//                // 현재 total_item이 기준(9000개)을 넘으면 Job Launcher로 Batch 작업 진행 메서드 호출
//                runNewsBatch(section);
//
//                // 중간 배치가 진행된 섹션 배치 현황 기록 (Redis에 횟수 증가)
//                intermediateBatchRedisService.incrementBatchCount(section);
//
//            } /* 2번째 중간 배치 부터 주의 메시지 호출 */
//            else if(total_items >= 18000){
//
//                // Job Launcher로 Batch 작업 진행 메서드 호출
//                runNewsBatch(section);
//
//                // 현재 total_item이 기준(18000개)을 넘으면 Job Launcher로 Batch 작업 진행 메서드 호출
//                log.info("[#주의#] {}섹션의 total_items수 : {}",section,total_items); // 추후, 하루에 3번 이상의 중간 호출이 일어나는 것에 대한 방어코드도 작성할 것
//
//            }
//        }
//    }

    // ======================= Spring Batch Job Launcher (Batch Starter Method) =========================

    /**
     * Spring Batch Job 실행 jobLauncher
     *
     * @param section 뉴스의 섹션
     */
    private void runNewsBatch(String section) {

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("section", section)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        try {
            /* 뉴스 모니터링 시 실행 될 Job 실행 */
            jobLauncher.run(newsDataSaveJob_Monitoring, jobParameters);
            log.info("[{}] Spring Batch Job 실행 완료", section);
        } catch (Exception e) {
            log.error("[{}] Spring Batch Job 실행 실패", section, e);
        }
    }

    // ======================= Test Method =========================

    /**
     * 섹션 별 뉴스 숫자 집계
     *
     */
    public void testMonitoring(){

        LocalDate day = LocalDate.now(); // 오늘의 날짜
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); //원하는 포맷 지정

        String dateTo = day.format(formatter); //문자열로 변환
        String dateFrom = day.format(formatter); //문자열로 변환

        log.info("오늘의 날짜 : {}", day); // 오늘의 날짜 로그 확인

        for (String section : sections) {

            // page_size=1로 호출해서 해당 섹션의 total_items를 모니터링
            int total_items = newsMonitoringManager.getTotalItems( section, dateFrom, dateTo);
            log.info("{} 영역 total_items : {}",section, total_items);

        }
    }
}
