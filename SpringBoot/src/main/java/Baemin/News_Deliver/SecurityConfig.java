package Baemin.News_Deliver;

import Baemin.News_Deliver.Global.JWT.JwtAuthenticationFilter;
import Baemin.News_Deliver.Global.OAuth2.CustomOAuth2UserService;
import Baemin.News_Deliver.Global.OAuth2.OAuth2LoginSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF 비활성화 (JWT 사용)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 무상태 설정 (JWT 사용)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        //  공개 접근 허용 (인증 불필요)
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/auth/status").permitAll()
                        .requestMatchers("/login/oauth2/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()

                        //  Swagger 및 API 문서
                        .requestMatchers("/swagger-ui/**", "/swagger.html", "/v3/api-docs/**", "/api-docs/**").permitAll()

                        //  정적 리소스
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                        //  헬스체크 및 모니터링
                        .requestMatchers("/actuator/health").permitAll()

                        //  테스트용 엔드포인트
                        .requestMatchers("/run-batch").permitAll()
                        .requestMatchers("/elasticsearch/**").permitAll()
                        .requestMatchers("/monitoring/test/**").permitAll()

                        //  HotTopic 공개 API (로그인 없이도 조회 가능)
                        .requestMatchers("/api/hottopic/**").permitAll()

                        //  카카오 메시지 발송 (스케줄러용 - 내부 호출만 허용하도록 IP 제한할 예정)
                        .requestMatchers("/kakao/send-message").permitAll()
                        .requestMatchers("/kakao/search-news").permitAll()
                        .requestMatchers("/kakao/getcron").permitAll()

                        //  JWT 인증 필요 - Auth 관련 API
                        .requestMatchers("/api/auth/refresh").permitAll() // 리프레시는 토큰으로 인증
                        .requestMatchers("/api/auth/logout", "/api/auth/me").authenticated()

                        //  JWT 인증 필요 - 사용자 설정 관리
                        .requestMatchers("/api/setting/**").authenticated()

                        //  JWT 인증 필요 - 서브 서비스들
                        .requestMatchers("/sub/**").authenticated()

                        //  기타 모든 API 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/kakao")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(401);
                            response.getWriter().write("{\"error\":\"OAuth2 Login Failed\",\"message\":\"" + exception.getMessage() + "\"}");
                        })
                )

                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 인증 실패 시 처리
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(401);
                            response.getWriter().write("{\"error\":\"Authentication Required\",\"message\":\"로그인이 필요합니다.\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(403);
                            response.getWriter().write("{\"error\":\"Access Denied\",\"message\":\"접근 권한이 없습니다.\"}");
                        })
                )

                .build();
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();


        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",           // 개발용 React
                "http://localhost:3001",           // 개발용 React (포트 변경시)
                "http://localhost:5173",           // 개발용 React
                "https://likelionnews.click",      // 프론트엔드 메인 도메인
                "https://www.likelionnews.click",  // 프론트엔드 www 도메인
                "https://*.amplifyapp.com",        // Amplify 기본 도메인
                "https://merry-crepe-479d93.netlify.app", // 기존 프로토타입
                "http://43.201.27.98"             // AWS EC2 IP
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}