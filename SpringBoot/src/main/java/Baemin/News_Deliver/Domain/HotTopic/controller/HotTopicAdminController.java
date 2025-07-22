package Baemin.News_Deliver.Domain.HotTopic.controller;

import Baemin.News_Deliver.Domain.HotTopic.service.HotTopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HotTopicAdminController {

    private final HotTopicService hotTopicService;

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
    @ApiResponses(@ApiResponse(responseCode = "204", description = "설정 삭제 성공"))
    @GetMapping("/api/admin/hottopic")
    public ResponseEntity<Void> saveHotTopic() throws IOException {
        hotTopicService.getAndSaveHotTopic();
        return ResponseEntity.noContent().build();
    }

}
