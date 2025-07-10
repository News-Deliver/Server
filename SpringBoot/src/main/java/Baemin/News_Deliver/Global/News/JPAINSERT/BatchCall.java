package Baemin.News_Deliver.Global.News.JPAINSERT;

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
