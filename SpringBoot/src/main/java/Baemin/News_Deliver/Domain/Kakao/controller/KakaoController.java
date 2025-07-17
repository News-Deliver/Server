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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/kakao")
public class KakaoController {

    private final KakaoMessageService kakaoMessageService;
    private final KakaoNewsService newsSearchService;
    private final KakaoSchedulerService kakaoSchedulerService;
    private final AuthRepository authRepository;


    //스케쥴러를 통한 메세지 전송코드
    //@GetMapping

    /**
     * 카카오톡 나에게 보내기 메시지 전송 메서드
     */
    @GetMapping("/send-message")
    public ResponseEntity<ApiResponseWrapper<String>> sendMessage() {

        //전체 유저의 정보를 받아, RefreshToken 가져옴.
        List<Auth> allUsers = authRepository.findAll();
        List<String> allUsersRefreshToken = allUsers.stream()
                .map(Auth::getKakaoRefreshToken)
                .toList();

        log.info("유저 RefreshToken 확인" + allUsersRefreshToken);


        // 1차 전송 실패시 저장되는 토큰
        List<String> failedTokens = new ArrayList<>();
        for (String token : allUsersRefreshToken) {
            try {
                Auth auth = authRepository.findByKakaoRefreshToken(token);
                Long userId = auth.getUser().getId();

                log.info("유저 정보 확인 코드 {}", userId);

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
            log.error("카카오 메시지 전송 실패 토큰 : {}", finalFailedTokens);
            throw new KakaoException(ErrorCode.MESSAGE_SEND_FAILED);
        }

        return ResponseEntity.ok(new ApiResponseWrapper<>("SUCCESS", "모든 유저에게 메시지 전송 성공"));
    }


    //테스트용 코드
    @GetMapping("/search-news")
    public ResponseEntity<List<NewsEsDocument>> searchNews() {
        List<String> keyword = null;
        List<String> blockKeyword = null;

        return ResponseEntity.ok(newsSearchService.searchNews(keyword, blockKeyword));
    }

    @GetMapping("/getcron")
    public ResponseEntity getcron() {
        Long userId = 1L;
        return ResponseEntity.ok(kakaoSchedulerService.getCron(userId));
    }
}