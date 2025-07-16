package Baemin.News_Deliver.Domain.HotTopic.controller;

import Baemin.News_Deliver.Domain.HotTopic.dto.HotTopicResponseDTO;
import Baemin.News_Deliver.Domain.HotTopic.entity.HotTopic;
import Baemin.News_Deliver.Domain.HotTopic.service.HotTopicService;
import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hottopic")
public class HotTopicController {

    private final HotTopicService hotTopicService;

    //ElasticSearch에서 "어제"의 핫토픽 추출
    @GetMapping
    public ResponseEntity<List<HotTopicResponseDTO>> getHotTopicList() {
        List<HotTopicResponseDTO> hotTopicResponseDTOList = hotTopicService.getHotTopicList();
        log.info("hotTopicResponseDTOList={}", hotTopicResponseDTOList.size());
        return ResponseEntity.ok(hotTopicResponseDTOList);
    }

    @GetMapping("/{keyword}")
    public ResponseEntity<List<NewsEsDocument>> getNewsList(@PathVariable("keyword") String keyword) throws IOException {
        List<NewsEsDocument> newsEsDocumentList = hotTopicService.getNewsList(keyword, 20);
        return ResponseEntity.ok(newsEsDocumentList);
    }

    //스케줄러로 처리 할 예정
    // FIXME
    //ElasticSearch에서 "어제"의 핫토픽 추출 > DB에 저장
    @GetMapping("/savehottopic")
    public void saveHotTopic() throws IOException {
        hotTopicService.getAndSaveHotTopic();
    }

}
