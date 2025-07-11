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

    @GetMapping("/bulk")
    public void bulk() {
        newsEsService.esBulkService();
    }
}
