package Baemin.News_Deliver.Domain.HotTopic.Exception;

import Baemin.News_Deliver.Global.Exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class HotTopicException extends RuntimeException {
    private final ErrorCode errorcode;
}
