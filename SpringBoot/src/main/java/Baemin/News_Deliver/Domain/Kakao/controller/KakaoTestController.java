package Baemin.News_Deliver.Domain.Kakao.controller;

import Baemin.News_Deliver.Domain.Auth.Entity.Auth;
import Baemin.News_Deliver.Domain.Auth.Repository.AuthRepository;
import Baemin.News_Deliver.Domain.Kakao.service.KakaoMessageService;
import Baemin.News_Deliver.Domain.Kakao.service.KakaoNewsService;
import Baemin.News_Deliver.Global.Kakao.KakaoTokenProvider;
import Baemin.News_Deliver.Global.ResponseObject.ApiResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class KakaoTestController {

    private final KakaoMessageService kakaoMessageService;
    private final KakaoTokenProvider provider;
    private final KakaoNewsService newsService;
    private final AuthRepository authRepository;

    /**
     * 카카오톡 나에게 보내기 메시지 전송 메서드
     */
    @GetMapping("/kakao/send-message")
    @ResponseBody
    public ResponseEntity<ApiResponseWrapper<String>> sendMessage() {

        try {
            boolean success = kakaoMessageService.sendKakaoMessage();

            if (success) {
                return ResponseEntity.ok(new ApiResponseWrapper<>("SUCCESS", "메시지 전송 성공"));
            } else {
                return ResponseEntity.internalServerError()
                        .body(new ApiResponseWrapper<>(null, "메시지 전송 실패"));
            }
        } catch (Exception e) {
            log.error("메시지 전송 중 오류 발생: ", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseWrapper<>(null, "메시지 전송 중 오류 발생: " + e.getMessage()));
        }
    }

}