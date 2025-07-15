package Baemin.News_Deliver.Domain.SubServices.MoreNews.Controller;

import Baemin.News_Deliver.Domain.SubServices.MoreNews.Service.MoreNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MoreNewsController {

    private final MoreNewsService moreNewsService;

    /* 뉴스 더보기 */
    public void getMoreNews() {


    }
}
