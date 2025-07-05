package Baemin.News_Deliver.Global.Exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /* Global 예외 0xx */
    DATABASE_ERROR("GLOBAL_ERROR_001",
            "데이터 베이스 오류",
            HttpStatus.INTERNAL_SERVER_ERROR);

    /* Auth 예외 : 6xx */
    /* HotTopic 예외 : 7xx */
    /* Kakao 예외 : 8xx */
    /* Mypage 예외 : 9xx */
    /* SubServices 예외 : 10xx */

    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;
}
