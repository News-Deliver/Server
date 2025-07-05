package Baemin.News_Deliver.Domain.Mypage.Exception;

import Baemin.News_Deliver.Global.Exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MypageException extends RuntimeException {
    private final ErrorCode errorcode;
}
