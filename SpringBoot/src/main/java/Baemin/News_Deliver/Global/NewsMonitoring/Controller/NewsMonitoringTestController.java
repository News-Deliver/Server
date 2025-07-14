package Baemin.News_Deliver.Global.NewsMonitoring.Controller;

import Baemin.News_Deliver.Global.NewsMonitoring.Service.IntermediateBatchRedisService;
import Baemin.News_Deliver.Global.NewsMonitoring.Service.NewsMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/monitoring/test/")
@RequiredArgsConstructor
@Tag(name = "뉴스 모니터링 테스트", description = "뉴스 모니터링 기능 테스트용 API입니다.")
public class NewsMonitoringTestController {

    /* News Monitoring System의 일부 기능들을 테스팅 할 수 있는 컨트롤러들이다.  */

    private final NewsMonitoringService newsMonitoringService;
    private final IntermediateBatchRedisService intermediateBatchRedisService;

    /**
     * 섹션 별 뉴스 숫자 집계 테스트 메서드
     *
     */
    @GetMapping("totalItems")
    @Operation(summary = "섹션 별 뉴스 숫자 집계", description = "Redis에 저장된 섹션별 total_items 수를 로그로 출력합니다.")
    public void getTotalItems() {

        newsMonitoringService.testMonitoring();
    }

    /* 예시 답변 */
//    politics 영역 total_items : 846
//    economy 영역 total_items : 742
//    society 영역 total_items : 2237
//    culture 영역 total_items : 799
//    tech 영역 total_items : 174
//    entertainment 영역 total_items : 2651
//    opinion 영역 total_items : 76

    /**
     * 자정이 되면 Redis에 있는 IntermediateBatch 중간 배치 기록 삭제 테스트 메서드
     *
     */
    @PostMapping("intermediate/flush")
    @Operation(summary = "중간 배치 키 삭제", description = "자정에 실행되어 Redis의 IntermediateBatch 키들을 삭제하는 테스트 API입니다.")
    public void flushIntermediateBatchKeys(){

        intermediateBatchRedisService.flushIntermediateBatchKeys();
    }

    /**
     * 섹션 별 중간 배치 작업 횟수를 조회하는 테스트 메서드
     *
     * @return 각 섹션 별 중간 배치 작업 횟수 반환
     */
    @GetMapping("intermediate/counts")
    @Operation(summary = "중간 배치 횟수 조회", description = "Redis에 저장된 각 섹션별 IntermediateBatch 횟수를 조회합니다.")
    public Map<String, Integer> getIntermediateBatchCounts() {

        return intermediateBatchRedisService.getAllBatchCountsForSections();
    }

    /* 예시 답변 */
//    {
//            "tech": 0,
//            "politics": 0,
//            "society": 4,
//            "entertainment": 4,
//            "culture": 0,
//            "economy": 0,
//            "opinion": 0
//    }


}
