package Baemin.News_Deliver.Domain.SubServices.MoreNews.Controller;

import Baemin.News_Deliver.Domain.SubServices.MoreNews.DTO.NewsHistoryResponse;
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
     * (페이지 네이션이 적용된) 내 히스토리 조회하기 API
     *
     *  요청 GET /api/news/history?page=1&size=5
     *
     * @param page 시작 페이지
     * @param size 한 페이지의 사이즈
     * @return 페이지 네이션이 적용된 뉴스 기사 리스트
     */
    @GetMapping("")
    public ResponseEntity<ApiResponseWrapper<List<NewsHistoryResponse>>> getNewsHistoryList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        List<NewsHistoryResponse> list = moreNewsService.getNewsHistoryList(page, size);

        return ResponseEntity.status(200)
                .body(new ApiResponseWrapper<>(list,"히스토리 조회 성공"));
    }
}
