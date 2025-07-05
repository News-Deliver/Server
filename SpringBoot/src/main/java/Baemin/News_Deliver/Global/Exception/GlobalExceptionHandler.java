package Baemin.News_Deliver.Global.Exception;

import Baemin.News_Deliver.Domain.Auth.Exception.AuthException;
import Baemin.News_Deliver.Domain.HotTopic.Exception.HotTopicException;
import Baemin.News_Deliver.Domain.Kakao.Exception.KakaoException;
import Baemin.News_Deliver.Domain.Mypage.Exception.MypageException;
import Baemin.News_Deliver.Domain.SubServices.Exception.SubServicesException;
import Baemin.News_Deliver.Global.ResponseObject.ApiResponseWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 인증&인가 시스템 예외
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponseWrapper<String>> authExceptions(AuthException ex) {
        ApiResponseWrapper<String> response = new ApiResponseWrapper<>(ex.getErrorcode().getErrorCode(),ex.getErrorcode().getMessage());
        return new ResponseEntity<>(response, ex.getErrorcode().getHttpStatus());
    }

    // HotTopic 도메인 예외
    @ExceptionHandler(HotTopicException.class)
    public ResponseEntity<ApiResponseWrapper<String>> hotTopicExceptions(HotTopicException ex) {
        ApiResponseWrapper<String> response = new ApiResponseWrapper<>(ex.getErrorcode().getErrorCode(),ex.getErrorcode().getMessage());
        return new ResponseEntity<>(response, ex.getErrorcode().getHttpStatus());
    }

    // Kakao 도메인 예외
    @ExceptionHandler(KakaoException.class)
    public ResponseEntity<ApiResponseWrapper<String>> kakoExceptions(KakaoException ex) {
        ApiResponseWrapper<String> response = new ApiResponseWrapper<>(ex.getErrorcode().getErrorCode(),ex.getErrorcode().getMessage());
        return new ResponseEntity<>(response, ex.getErrorcode().getHttpStatus());
    }

    // Mypage 도메인 예외
    @ExceptionHandler(MypageException.class)
    public ResponseEntity<ApiResponseWrapper<String>> mypageExceptions(MypageException ex) {
        ApiResponseWrapper<String> response = new ApiResponseWrapper<>(ex.getErrorcode().getErrorCode(),ex.getErrorcode().getMessage());
        return new ResponseEntity<>(response, ex.getErrorcode().getHttpStatus());
    }

    // SubServices 도메인 예외
    @ExceptionHandler(SubServicesException.class)
    public ResponseEntity<ApiResponseWrapper<String>> subServicesExceptions(SubServicesException ex) {
        ApiResponseWrapper<String> response = new ApiResponseWrapper<>(ex.getErrorcode().getErrorCode(),ex.getErrorcode().getMessage());
        return new ResponseEntity<>(response, ex.getErrorcode().getHttpStatus());
    }

}
