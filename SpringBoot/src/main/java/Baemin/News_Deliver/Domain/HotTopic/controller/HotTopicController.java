package Baemin.News_Deliver.Domain.HotTopic.controller;

import Baemin.News_Deliver.Domain.HotTopic.dto.HotTopicResponseDTO;
import Baemin.News_Deliver.Domain.HotTopic.service.HotTopicService;
import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * 핫토픽 API 컨트롤러
 *
 * <p>Elasticsearch에서 추출된 어제의 인기 키워드(핫토픽) 및 연관 뉴스 데이터를 제공하는 API입니다.</p>
 *
 * 주요 기능:
 * <ul>
 *     <li>어제 핫토픽 Top 10 조회</li>
 *     <li>특정 키워드로 연관 뉴스 조회</li>
 *     <li>핫토픽 추출 및 저장 (스케줄 예정)</li>
 * </ul>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hottopic")
public class HotTopicController {

    private final HotTopicService hotTopicService;

    /**
     * 어제의 핫토픽 Top 10 조회 API
     *
     * @return 핫토픽 리스트 (최대 10개)
     */
    @Operation(summary = "어제의 핫토픽 조회", description = "Elasticsearch에서 추출된 어제의 인기 키워드 Top 10을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "정상적으로 핫토픽 목록을 반환했습니다.")
    @GetMapping
    public ResponseEntity<List<HotTopicResponseDTO>> getHotTopicList() {
        List<HotTopicResponseDTO> hotTopicResponseDTOList = hotTopicService.getHotTopicList();
        return ResponseEntity.ok(hotTopicResponseDTOList);
    }

    /**
     * 특정 키워드로 연관 뉴스 기사 조회 API
     *
     * @param keyword 검색 키워드 (예: 윤석열)
     * @return 해당 키워드와 연관된 뉴스 기사 리스트
     * @throws IOException Elasticsearch 통신 오류
     */
    @Operation(summary = "키워드 기반 뉴스 조회", description = "특정 키워드로 관련 뉴스 기사 20개를 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "뉴스 리스트 반환 성공"),
            @ApiResponse(responseCode = "500", description = "Elasticsearch 오류")
    })
    @GetMapping("/{keyword}")
    public ResponseEntity<List<NewsEsDocument>> getNewsList(
            @Parameter(description = "검색 키워드", example = "윤석열")
            @PathVariable("keyword") String keyword) throws IOException {

        List<NewsEsDocument> newsEsDocumentList = hotTopicService.getNewsList(keyword, 20);
        return ResponseEntity.ok(newsEsDocumentList);
    }

    /**
     * 어제의 핫토픽 키워드를 Elasticsearch에서 추출 후 DB에 저장
     *
     * <p>내부 테스트용이거나 추후 스케줄러로 대체될 예정입니다.</p>
     *
     * @throws IOException Elasticsearch 통신 오류
     */
    @Operation(
            summary = "어제의 핫토픽 추출 및 저장 (관리용)",
            description = "Elasticsearch에서 어제의 인기 키워드를 추출하고 DB에 저장합니다. (스케줄러 예정)"
    )
    @ApiResponse(responseCode = "200", description = "저장 완료")
    @GetMapping("/savehottopic")
    public void saveHotTopic() throws IOException {
        hotTopicService.getAndSaveHotTopic();
    }
}
