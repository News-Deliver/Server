package Baemin.News_Deliver.Domain.Kakao.controller;

import Baemin.News_Deliver.Domain.Kakao.service.KakaoMessageService;
import Baemin.News_Deliver.Domain.Kakao.service.KakaoNewsService;
import Baemin.News_Deliver.Global.ResponseObject.ApiResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class KakaoTestController {

    private final KakaoMessageService kakaoMessageService;
    private final KakaoNewsService newsService;

    /**
     * 카카오톡 나에게 보내기 메시지 전송 테스트용
     */
    @PostMapping("/kakao/send-message")
    @ResponseBody
    public ResponseEntity<ApiResponseWrapper<String>> sendMessage(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponseWrapper<>(null, "메시지 내용이 비어있습니다."));
            }

            boolean success = kakaoMessageService.sendKakaoMessage(message);

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

    /**
     * 뉴스 데이터를 이용한 카카오톡 메시지 전송
     */
    @PostMapping("/kakao/send-news")
    @ResponseBody
    public ResponseEntity<ApiResponseWrapper<String>> sendNewsMessage() {
        try {
            String newsMessage = newsService.getRandomNewsMessage();
            boolean success = kakaoMessageService.sendKakaoMessage(newsMessage);

            if (success) {
                return ResponseEntity.ok(new ApiResponseWrapper<>("SUCCESS", "뉴스 메시지 전송 성공"));
            } else {
                return ResponseEntity.internalServerError()
                        .body(new ApiResponseWrapper<>(null, "뉴스 메시지 전송 실패"));
            }
        } catch (Exception e) {
            log.error("뉴스 메시지 전송 중 오류 발생: ", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseWrapper<>(null, "뉴스 메시지 전송 중 오류 발생: " + e.getMessage()));
        }
    }


    /**
     * 테스트용 뉴스 메시지 전송 엔드포인트
     */
    @GetMapping("/kakao/test-news")
    @ResponseBody
    public ResponseEntity<ApiResponseWrapper<String>> sendTestNewsMessage() {
        try {
            String newsMessage = newsService.getRandomNewsMessage();
            boolean success = kakaoMessageService.sendKakaoMessage(newsMessage);

            if (success) {
                return ResponseEntity.ok(new ApiResponseWrapper<>("SUCCESS", "테스트 뉴스 메시지 전송 성공"));
            } else {
                return ResponseEntity.internalServerError()
                        .body(new ApiResponseWrapper<>(null, "테스트 뉴스 메시지 전송 실패"));
            }
        } catch (Exception e) {
            log.error("테스트 뉴스 메시지 전송 중 오류 발생: ", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseWrapper<>(null, "테스트 뉴스 메시지 전송 중 오류 발생: " + e.getMessage()));
        }
    }
}