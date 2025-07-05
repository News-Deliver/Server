package Baemin.News_Deliver.Domain.SubServices.Exception;

import Baemin.News_Deliver.Global.Exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SubServicesException extends RuntimeException {
    private final ErrorCode errorcode;
}
