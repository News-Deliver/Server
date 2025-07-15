package Baemin.News_Deliver.Global.News.ElasticSearch.controller;

import Baemin.News_Deliver.Global.News.ElasticSearch.service.NewsEsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/elasticsearch")
public class EsCallController {

    private final NewsEsService newsEsService;

    //스케줄러로 구현할 예정
    //ADMIN(관리자)기능이 추가된다면 유지할 여지가 있음
    //FIXME
    @GetMapping("/bulk")
    public void bulk() {
        newsEsService.esBulkService();
    }
}
