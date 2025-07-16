package Baemin.News_Deliver.Domain.SubServices.MoreNews.Controller;

import Baemin.News_Deliver.Domain.SubServices.MoreNews.Service.MoreNewsService;
import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import Baemin.News_Deliver.Global.ResponseObject.ApiResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sub/more")
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
}
