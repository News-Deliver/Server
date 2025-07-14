package Baemin.News_Deliver.Domain.HotTopic.controller;

import Baemin.News_Deliver.Domain.HotTopic.service.HotTopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hottopic")
public class HotTopicController {

    private final HotTopicService hotTopicService;

    //스케줄러로 처리 할 예정
    @GetMapping("/savehottopic")
    public void getTopic() {
        hotTopicService.getAndSaveHotTopic();
    }

    @GetMapping("/gethottopic")
    public void getHotTopic() {
        try {
            hotTopicService.logRelatedArticlesForHotTopics();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
