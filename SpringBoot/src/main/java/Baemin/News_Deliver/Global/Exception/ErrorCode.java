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
            HttpStatus.INTERNAL_SERVER_ERROR),

    /* Auth 예외: 6xx */
    // 사용자 관련 (601~610)
    USER_NOT_FOUND("AUTH_ERROR_601", "사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    USER_CREATION_FAILED("AUTH_ERROR_602", "사용자 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_UNAUTHORIZED("AUTH_ERROR_603", "사용자 권한이 없습니다", HttpStatus.FORBIDDEN),

    // JWT 토큰 관련 (611~620)
    INVALID_TOKEN("AUTH_ERROR_611", "유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("AUTH_ERROR_612", "만료된 토큰입니다", HttpStatus.UNAUTHORIZED),
    TOKEN_REFRESH_FAILED("AUTH_ERROR_613", "토큰 갱신에 실패했습니다", HttpStatus.UNAUTHORIZED),
    TOKEN_STORAGE_FAILED("AUTH_ERROR_614", "토큰 저장 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    TOKEN_NOT_PROVIDED("AUTH_ERROR_615", "토큰이 제공되지 않았습니다", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_INVALID("AUTH_ERROR_616", "리프레시 토큰이 유효하지 않습니다", HttpStatus.UNAUTHORIZED),

    // 인증/로그인 관련 (621~630)
    LOGIN_FAILED("AUTH_ERROR_621", "로그인에 실패했습니다", HttpStatus.UNAUTHORIZED),
    LOGOUT_FAILED("AUTH_ERROR_622", "로그아웃에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    AUTHENTICATION_REQUIRED("AUTH_ERROR_623", "인증이 필요합니다", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_FAILED("AUTH_ERROR_624", "인증에 실패했습니다", HttpStatus.UNAUTHORIZED),

    // OAuth2/카카오 관련 (631~640)
    KAKAO_LOGIN_FAILED("AUTH_ERROR_631", "카카오 로그인에 실패했습니다", HttpStatus.UNAUTHORIZED),
    KAKAO_TOKEN_REFRESH_FAILED("AUTH_ERROR_632", "카카오 토큰 갱신에 실패했습니다", HttpStatus.UNAUTHORIZED),
    KAKAO_TOKEN_INVALID("AUTH_ERROR_633", "카카오 토큰이 유효하지 않습니다", HttpStatus.UNAUTHORIZED),
    OAUTH2_PROVIDER_NOT_SUPPORTED("AUTH_ERROR_634", "지원하지 않는 OAuth2 제공자입니다", HttpStatus.BAD_REQUEST),
    OAUTH2_PROCESS_FAILED("AUTH_ERROR_635", "OAuth2 처리 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),



    // Setting 관련
    SETTING_NOT_FOUND("SETTING_ERROR_901", "설정을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    SETTING_ACCESS_DENIED("SETTING_ERROR_902", "다른 사용자의 설정에 접근할 수 없습니다", HttpStatus.FORBIDDEN),
    SETTING_CREATION_FAILED("SETTING_ERROR_903", "설정 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    SETTING_UPDATE_FAILED("SETTING_ERROR_904", "설정 수정에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    SETTING_DELETE_FAILED("SETTING_ERROR_905", "설정 삭제에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    /* HotTopic 예외 : 7xx */



    /* Kakao 예외 : 8xx */
    MESSAGE_SEND_FAILED("KAKAO_ERROR_801", "카카오 메시지 전송 실패", HttpStatus.INTERNAL_SERVER_ERROR),


    /* Mypage 예외 : 9xx */
    /* SubServices 예외 : 10xx */
    HISTORY_NOT_FOUND("SUB_ERROR_1001","히스토리 객체를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    FEEDBACK_NOT_FOUND("SUB_ERROR_1051","피드백 객체를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),;

    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;
}