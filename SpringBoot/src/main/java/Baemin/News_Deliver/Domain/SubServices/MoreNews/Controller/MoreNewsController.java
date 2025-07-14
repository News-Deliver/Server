package Baemin.News_Deliver.Domain.SubServices.MoreNews.Controller;

import Baemin.News_Deliver.Domain.SubServices.Exception.SubServicesException;
import Baemin.News_Deliver.Domain.SubServices.MoreNews.DTO.MoreNewsDTO;
import Baemin.News_Deliver.Domain.SubServices.MoreNews.Service.MoreNewsService;
import Baemin.News_Deliver.Global.ResponseObject.ApiResponseWrapper;
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
public class MoreNewsController {

    private final MoreNewsService moreNewsService;

    /**
     * 뉴스 더보기 API
     *
     * @param historyId 더보기를 요청한 히스토리 ID (쿼리 또는 경로 파라미터)
     * @return 뉴스 리스트(최대 25개)
     */
    @GetMapping("/{historyId}")
    public ResponseEntity<ApiResponseWrapper<List<MoreNewsDTO>>> getMoreNews(@PathVariable Long historyId) {
        try {
            List<MoreNewsDTO> moreNewsList = moreNewsService.searchMoreNews(historyId);
            ApiResponseWrapper<List<MoreNewsDTO>> response = new ApiResponseWrapper<>(moreNewsList, "뉴스 조회 성공");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            ApiResponseWrapper<List<MoreNewsDTO>> errorResponse = new ApiResponseWrapper<>(null, "서버 오류 발생");
            errorResponse.setErrorCode("IO_EXCEPTION");
            return ResponseEntity.status(500).body(errorResponse);
        } catch (Exception e) {
            ApiResponseWrapper<List<MoreNewsDTO>> errorResponse = new ApiResponseWrapper<>(null, "알 수 없는 오류 발생");
            errorResponse.setErrorCode("UNKNOWN_ERROR");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

}

