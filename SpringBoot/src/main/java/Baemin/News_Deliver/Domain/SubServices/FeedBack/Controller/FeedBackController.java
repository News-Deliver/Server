package Baemin.News_Deliver.Domain.SubServices.FeedBack.Controller;

import Baemin.News_Deliver.Domain.SubServices.FeedBack.DTO.FeedbackRequest;
import Baemin.News_Deliver.Domain.SubServices.FeedBack.Service.FeedBackService;
import Baemin.News_Deliver.Global.ResponseObject.ApiResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sub/feedback")
@Tag(name = "Sub/FeedBack", description = "서브/피드백 API ")
public class FeedBackController {

    private final FeedBackService feedBackService;

    /**
     * 키워드 반영도 피드백 API
     *
     * @param request 피드백 요청 DTO
     * @return 피드백 결과 반환
     */
    @Operation(summary = "키워드 반영도 피드백 API", description = "유저가 각 히스토리 별 피드백 작업을 진행하는 기능이다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "요청하신 피드백 작업이 정상적으로 처리 되었습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @PostMapping("/keyword")
    public ResponseEntity<ApiResponseWrapper<Long>> keywordFeedBack(@RequestBody FeedbackRequest request) {

        Long result = feedBackService.keywordFeedBack(request);

        return ResponseEntity.status(HttpStatus.OK).
                body(new ApiResponseWrapper<>(result,"요청하신 피드백 작업이 정상적으로 처리 되었습니다."));
    }


}
