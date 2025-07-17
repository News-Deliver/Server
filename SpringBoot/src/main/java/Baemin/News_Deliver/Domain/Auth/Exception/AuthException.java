package Baemin.News_Deliver.Domain.Auth.Exception;

import Baemin.News_Deliver.Global.Exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuthException extends RuntimeException {
    private final ErrorCode errorcode;

    @Override
    public String getMessage() {
        return errorcode.getMessage();
    }
}