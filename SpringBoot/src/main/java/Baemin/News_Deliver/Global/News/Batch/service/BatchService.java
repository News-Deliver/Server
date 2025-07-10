package Baemin.News_Deliver.Global.News.Batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BatchService {

    private final JobLauncher jobLauncher;
    private final Job newsDataSaveJob;

    //sections의 값들
    private  String[] sections = {"politics", "economy", "society", "culture", "tech", "entertainment", "opinion"};

    public ResponseEntity<String> runBatch() {
        try {
            long totalStart = System.currentTimeMillis(); // 전체 시작 시간

            for (String section : sections) {
                JobParameters params = new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .addString("section", section)
                        .toJobParameters();

                log.info("📦 섹션별 배치 시작: {}", section);
                jobLauncher.run(newsDataSaveJob, params);
            }

            long totalEnd = System.currentTimeMillis(); // 전체 끝 시간
            log.info("✅ 전체 섹션 배치 소요 시간: {} ms", (totalEnd - totalStart));

            return ResponseEntity.ok("Batch Job Started for all sections");
        } catch (Exception e) {
            log.error("❌ Batch Job Failed", e);
            return ResponseEntity.status(500).body("Batch Job Failed");
        }
    }

}
