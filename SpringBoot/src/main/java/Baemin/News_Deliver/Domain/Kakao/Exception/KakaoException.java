package Baemin.News_Deliver.Domain.Kakao.Exception;

import Baemin.News_Deliver.Global.Exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class KakaoException extends RuntimeException {
    private final ErrorCode errorcode;
}
