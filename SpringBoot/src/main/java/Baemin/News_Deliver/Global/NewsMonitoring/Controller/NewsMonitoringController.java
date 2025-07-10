package Baemin.News_Deliver.Global.NewsMonitoring.Controller;

import Baemin.News_Deliver.Global.NewsMonitoring.Service.NewsMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/monitoring")
@RequiredArgsConstructor
public class NewsMonitoringController {

    private final NewsMonitoringService newsMonitoringService;

    /**
     * 섹션 별 뉴스 숫자 집계
     *
     */
    @GetMapping("/test")
    public void testMonitoring  () {

        newsMonitoringService.testMonitoring();
    }

    /**
     * Test 결과
     *
     * 2025-07-09T21:28:27.312Z  INFO 1 --- [News-Deliver] [nio-8080-exec-2] B.N.G.N.Service.NewsMonitoringService    : 오늘의 날짜 : 2025-07-09
     * 2025-07-09T21:28:29.416Z  INFO 1 --- [News-Deliver] [nio-8080-exec-2] B.N.G.N.Service.NewsMonitoringService    : [politics] total_items: 3820
     * 2025-07-09T21:28:29.416Z  INFO 1 --- [News-Deliver] [nio-8080-exec-2] B.N.G.N.Service.NewsMonitoringService    : [politics] 수집된 total_items: 3820
     * 2025-07-09T21:28:30.198Z  INFO 1 --- [News-Deliver] [nio-8080-exec-2] B.N.G.N.Service.NewsMonitoringService    : [economy] total_items: 6041
     * 2025-07-09T21:28:30.199Z  INFO 1 --- [News-Deliver] [nio-8080-exec-2] B.N.G.N.Service.NewsMonitoringService    : [economy] 수집된 total_items: 6041
     * 2025-07-09T21:28:31.167Z  INFO 1 --- [News-Deliver] [nio-8080-exec-2] B.N.G.N.Service.NewsMonitoringService    : [society] total_items: 11281
     * 2025-07-09T21:28:31.168Z  INFO 1 --- [News-Deliver] [nio-8080-exec-2] B.N.G.N.Service.NewsMonitoringService    : [society] 수집된 total_items: 11281
     * 2025-07-09T21:28:32.153Z  INFO 1 --- [News-Deliver] [nio-8080-exec-2] B.N.G.N.Service.NewsMonitoringService    : [culture] total_items: 2242
     * 2025-07-09T21:28:32.154Z  INFO 1 --- [News-Deliver] [nio-8080-exec-2] B.N.G.N.Service.NewsMonitoringService    : [culture] 수집된 total_items: 2242
     * 2025-07-09T21:28:33.066Z  INFO 1 --- [News-Deliver] [nio-8080-exec-2] B.N.G.N.Service.NewsMonitoringService    : [tech] total_items: 1368
     * 2025-07-09T21:28:33.067Z  INFO 1 --- [News-Deliver] [nio-8080-exec-2] B.N.G.N.Service.NewsMonitoringService    : [tech] 수집된 total_items: 1368
     * 2025-07-09T21:28:33.890Z  INFO 1 --- [News-Deliver] [nio-8080-exec-2] B.N.G.N.Service.NewsMonitoringService    : [entertainment] total_items: 7650
     * 2025-07-09T21:28:33.892Z  INFO 1 --- [News-Deliver] [nio-8080-exec-2] B.N.G.N.Service.NewsMonitoringService    : [entertainment] 수집된 total_items: 7650
     * 2025-07-09T21:28:35.015Z  INFO 1 --- [News-Deliver] [nio-8080-exec-2] B.N.G.N.Service.NewsMonitoringService    : [opinion] total_items: 284
     * 2025-07-09T21:28:35.016Z  INFO 1 --- [News-Deliver] [nio-8080-exec-2] B.N.G.N.Service.NewsMonitoringService    : [opinion] 수집된 total_items: 284
     *
     */

}
