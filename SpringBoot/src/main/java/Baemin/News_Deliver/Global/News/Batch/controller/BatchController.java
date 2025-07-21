package Baemin.News_Deliver.Global.News.Batch.controller;

import Baemin.News_Deliver.Global.News.Batch.service.BatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParametersInvalidException;
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

    private final BatchService batchService;

    @Operation(
            summary = "배치 작업 실행",
            description = "관리자 권한으로 뉴스 데이터를 배치 처리합니다. 실행 중이면 예외가 발생할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "성공적으로 배치 작업이 실행됨"),
            @ApiResponse(responseCode = "500", description = "배치 실행 중 오류 발생 (Job이 이미 실행 중이거나 완료됨)")
    })
    @GetMapping("/api/admin/batch")
    public ResponseEntity<Void> runBatch()
            throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException,
            JobRestartException {
        batchService.runBatch();
        return ResponseEntity.noContent().build();
    }
}