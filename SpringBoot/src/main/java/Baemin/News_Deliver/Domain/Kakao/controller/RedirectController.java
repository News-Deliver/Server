package Baemin.News_Deliver.Domain.Kakao.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Controller
public class RedirectController {

    /**
     * 메시지를 전송 받은 유저가, 뉴스 기사를 클릭하면 리다이렉트를 진행해주는 컨트롤러
     *
     * @param target 타깃 url (유저가 누른 뉴스기사의 url)
     * @param response 응답
     * @throws IOException IO 예외
     */
    @GetMapping("/redirect")
    public void redirectToTarget(@RequestParam("target") String target, HttpServletResponse response) throws IOException {

        // URL 디코딩 (이미 인코딩된 URL이 들어오므로 디코딩 후 사용)
        String decodedUrl = URLDecoder.decode(target, StandardCharsets.UTF_8);

        // 유저가 입장한 페이지 로깅
        log.info("Redirecting to external URL: {}", decodedUrl);

        // 리다이렉트 수행
        response.sendRedirect(decodedUrl);
    }
}
