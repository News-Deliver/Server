package Baemin.News_Deliver.Domain.HotTopic.controller;

import Baemin.News_Deliver.Domain.HotTopic.dto.HotTopicResponseDTO;
import Baemin.News_Deliver.Domain.HotTopic.entity.HotTopic;
import Baemin.News_Deliver.Domain.HotTopic.service.HotTopicService;
import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hottopic")
public class HotTopicController {

    private final HotTopicService hotTopicService;

    //TODO : Redis 캐시에 올릴 예정
    // TODO : 전역 예외 처리 컨트롤러 추가
    @GetMapping
    public ResponseEntity<List<HotTopicResponseDTO>> getHotTopicList() {
        List<HotTopicResponseDTO> hotTopicResponseDTOList = hotTopicService.getHotTopicList();
        return ResponseEntity.ok(hotTopicResponseDTOList);
    }

    @GetMapping("/{keyword}")
    public ResponseEntity<List<NewsEsDocument>> getNewsList(@PathVariable("keyword") String keyword) throws IOException {
        List<NewsEsDocument> newsEsDocumentList = hotTopicService.getNewsList(keyword, 20);
        return ResponseEntity.ok(newsEsDocumentList);
    }

    //스케줄러로 처리 할 예정
    // FIXME
    @GetMapping("/savehottopic")
    public void saveHotTopic() throws IOException {
        hotTopicService.getAndSaveHotTopic();
    }

}
