<<<<<<<< HEAD:SpringBoot/src/main/java/Baemin/News_Deliver/Global/News/JPAINSERT/BatchCall.java
package Baemin.News_Deliver.Global.News.JPAINSERT;
========
package Baemin.News_Deliver.Global.News.service;
>>>>>>>> origin/dev:SpringBoot/src/main/java/Baemin/News_Deliver/Global/News/service/BatchCall.java

import Baemin.News_Deliver.Global.News.JPAINSERT.service.BatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BatchCall {

    private final BatchService batchService;

    @GetMapping("/batch")
    public void batch() {
        batchService.batch();
    }
}
