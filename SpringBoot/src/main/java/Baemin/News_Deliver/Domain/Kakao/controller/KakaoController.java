package Baemin.News_Deliver.Domain.Kakao.controller;

import Baemin.News_Deliver.Domain.Auth.Entity.Auth;
import Baemin.News_Deliver.Domain.Auth.Repository.AuthRepository;
import Baemin.News_Deliver.Domain.Kakao.Exception.KakaoException;
import Baemin.News_Deliver.Domain.Kakao.service.KakaoMessageService;
import Baemin.News_Deliver.Domain.Kakao.service.KakaoNewsService;
import Baemin.News_Deliver.Domain.Kakao.service.KakaoSchedulerService;
import Baemin.News_Deliver.Global.Exception.ErrorCode;
import Baemin.News_Deliver.Global.News.ElasticSearch.dto.NewsEsDocument;
import Baemin.News_Deliver.Global.ResponseObject.ApiResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 카카오 메시지 및 뉴스 관련 API 컨트롤러
 * <p>사용자로부터 키워드를 제공받아, 사용자별 맞춤 뉴스 데이터를 카카오톡으로 전달합니다.</p>
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/kakao")
@Tag(name = "Kakao API", description = "카카오톡 뉴스 메시지 API")
public class KakaoController {

    private final KakaoMessageService kakaoMessageService;
    private final KakaoNewsService newsSearchService;
    private final KakaoSchedulerService kakaoSchedulerService;
    private final AuthRepository authRepository;

    /**
     * 모든 유저에게 카카오톡 메시지를 전송하는 API
     *
     * 코드 호출 순서
     * <ul>
     *     <li>카카오톡으로 회원 가입한 전체 유저의 정보를 받아, 전체 유저의 RefreshToken 가져옵니다.</li>
     *     <li>유저의 RefreshToken을 기준으로 각 유저의 정보를 받아옵니다.</li>
     *     <li>각 유저별로 kakaoMessageService 의 sendKakaoMessage를 호출합니다.</li>
     * </ul>
     *
     * @return 메시지 전송 결과 응답
     */
    @Operation(
            summary = "카카오톡 메시지 전송",
            description = "전체 유저의 카카오 리프레시 토큰을 기준으로 카카오톡 메시지를 전송합니다. 실패한 토큰은 두 번까지 재시도합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "모든 유저에게 메시지 전송 성공"),
                    @ApiResponse(responseCode = "500", description = "카카오 메시지 전송 실패 (최종 실패 토큰 존재)")
            }
    )
    @GetMapping("/send-message")
    public ResponseEntity<ApiResponseWrapper<String>> sendMessage() {

        // 전체 유저의 정보를 받아, RefreshToken 가져옴.
        List<Auth> allUsers = authRepository.findAll();
        List<String> allUsersRefreshToken = allUsers.stream()
                .map(Auth::getKakaoRefreshToken)
                .toList();

        log.info("유저 RefreshToken 확인: {}", allUsersRefreshToken);

        // 1차 전송 실패시 저장되는 토큰
        List<String> failedTokens = new ArrayList<>();
        for (String token : allUsersRefreshToken) {
            try {
                Auth auth = authRepository.findByKakaoRefreshToken(token);
                Long userId = auth.getUser().getId();
                log.info("유저 정보 확인: {}", userId);
                kakaoMessageService.sendKakaoMessage(token, userId);
            } catch (KakaoException e) {
                failedTokens.add(token);
            }
        }

        // 2차 실패 토큰 (2차 시도까지 실패한 경우만)
        List<String> finalFailedTokens = new ArrayList<>();
        for (String token : failedTokens) {
            try {
                Auth auth = authRepository.findByKakaoRefreshToken(token);
                Long userId = auth.getUser().getId();
                kakaoMessageService.sendKakaoMessage(token, userId);
            } catch (KakaoException e) {
                finalFailedTokens.add(token);
            }
        }

        if (!finalFailedTokens.isEmpty()) {
            log.error("카카오 메시지 전송 실패 토큰: {}", finalFailedTokens);
            throw new KakaoException(ErrorCode.MESSAGE_SEND_FAILED);
        }

        return ResponseEntity.ok(new ApiResponseWrapper<>("SUCCESS", "모든 유저에게 메시지 전송 성공"));
    }

    /**
     * 키워드 기반 뉴스 검색 테스트 API
     *
     * @return 뉴스 검색 결과 리스트
     */
    @Operation(
            summary = "뉴스 검색 테스트",
            description = "키워드와 차단 키워드를 기반으로 어제 날짜의 뉴스를 검색합니다. (현재는 테스트용으로 키워드는 null 전달)"
    )
    @GetMapping("/search-news-test")
    public ResponseEntity<List<NewsEsDocument>> searchNewsTest() {
        List<String> keyword = null;
        List<String> blockKeyword = null;

        return ResponseEntity.ok(newsSearchService.searchNews(keyword, blockKeyword));
    }
}
