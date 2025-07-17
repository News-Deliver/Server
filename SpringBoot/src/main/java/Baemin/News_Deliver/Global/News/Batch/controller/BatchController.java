package Baemin.News_Deliver.Global.News.Batch.controller;

import Baemin.News_Deliver.Global.News.Batch.service.BatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job newsDataSaveJob;
    private final BatchService batchService;

    @GetMapping("/run-batch") // ✅ GET 방식으로 변경
    public ResponseEntity<String> runBatch() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return batchService.runBatch();
    }
}