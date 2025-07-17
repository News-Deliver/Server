package Baemin.News_Deliver.Global.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger springdoc-ui 구성 파일
 */
@Configuration
public class SwaggerConfig {

    /* Swagger 설정 Bean */
    // 접근 경로 : http://localhost:8080/swagger-ui/index.html
    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Lion News Deliver API Document")
                .version("v0.0.1")
                .description("배달의 민족 팀 : 멋쟁이 뉴스 배달부의 API 명세서");

        return new OpenAPI()
                .components(new Components())
                .info(info);
    }
}
