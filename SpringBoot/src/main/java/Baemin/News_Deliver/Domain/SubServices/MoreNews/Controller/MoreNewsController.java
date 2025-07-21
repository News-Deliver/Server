package Baemin.News_Deliver.Domain.SubServices.MoreNews.Controller;

import Baemin.News_Deliver.Domain.SubServices.MoreNews.DTO.GroupedNewsHistoryResponse;
import Baemin.News_Deliver.Domain.SubServices.MoreNews.Service.MoreNewsService;
import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import Baemin.News_Deliver.Global.ResponseObject.ApiResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sub/history")
@Tag(name = "Sub/More-News", description = "서브 / 뉴스 더보기 API")
public class MoreNewsController {

    private final MoreNewsService moreNewsService;

    /**
     * 뉴스 더보기 API
     *
     * @param historyId 히스토리 고유 번호
     * @return 10개의 추가적인 뉴스 기사 리스트
     * @throws IOException 예외 처리
     */
    @Operation(summary = "뉴스 더보기 API",description = "해당 History의 고유 번호를 입력 받은 후, 추가적인 검색을 통해 10개의 리스트를 더 반환 ",
            responses = {
                    @ApiResponse(responseCode = "201",description = "뉴스 기사가 정상적으로 추가 반환되었습니다."),
                    @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
                    @ApiResponse(responseCode = "500",description = "서버 내부 오류 발생")
            })
    @GetMapping("/{historyId}")
    public ResponseEntity<ApiResponseWrapper<List<NewsEsDocument>>> getMoreNews(@PathVariable Long historyId) throws IOException {

        // 뉴스 더보기 서비스 레이어 호출
        List<NewsEsDocument> newsList = moreNewsService.getMoreNews(historyId);

        return ResponseEntity.status(201)
                .body(new ApiResponseWrapper<>(newsList,"뉴스 기사가 정상적으로 추가 반환되었습니다."));

    }

    /**
     * 내 히스토리 조회하기 API
     *
     * @param page 시작 페이지
     * @param size 페이지 사이즈
     * @return 페이지 네이션이 적용된 히스토리
     */
    @Operation(summary = "내 히스토리 조회하기 API",description = "히스토리 리스트를 반환(페이지 네이션 적용)",
            responses = {
                    @ApiResponse(responseCode = "201",description = "히스토리가 성공적으로 조회되었습니다."),
                    @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
                    @ApiResponse(responseCode = "500",description = "서버 내부 오류 발생")
            })
    @GetMapping("")
    public ResponseEntity<ApiResponseWrapper<List<GroupedNewsHistoryResponse>>> getNewsHistoryList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size
    ) {
        // 내 히스토리 조회하기 서비스 레이어 호출
        List<GroupedNewsHistoryResponse> groupedList = moreNewsService.getGroupedNewsHistory(page, size);

        return ResponseEntity.ok(new ApiResponseWrapper<>(groupedList, "히스토리가 성공적으로 조회되었습니다."));
    }

}
