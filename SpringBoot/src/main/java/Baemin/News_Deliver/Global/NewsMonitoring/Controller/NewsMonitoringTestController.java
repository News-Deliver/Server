package Baemin.News_Deliver.Global.NewsMonitoring.Controller;

import Baemin.News_Deliver.Global.NewsMonitoring.Service.IntermediateBatchRedisService;
import Baemin.News_Deliver.Global.NewsMonitoring.Service.NewsMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/monitoring/test/")
@RequiredArgsConstructor
public class NewsMonitoringTestController {

    /* News Monitoring System의 일부 기능들을 테스팅 할 수 있는 컨트롤러들이다.  */

    private final NewsMonitoringService newsMonitoringService;
    private final IntermediateBatchRedisService intermediateBatchRedisService;

    /**
     * 섹션 별 뉴스 숫자 집계 테스트 메서드
     *
     */
    @GetMapping("totalItems")
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
    public void flushIntermediateBatchKeys(){

        intermediateBatchRedisService.flushIntermediateBatchKeys();
    }

    /**
     * 섹션 별 중간 배치 작업 횟수를 조회하는 테스트 메서드
     *
     * @return 각 섹션 별 중간 배치 작업 횟수 반환
     */
    @GetMapping("intermediate/counts")
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
