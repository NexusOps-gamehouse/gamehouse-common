package gg.duo.common.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 서비스 공통 보안 설정.
 *
 * 각 서비스가 @Configuration @EnableWebSecurity 를 붙여 이 클래스를 상속하고,
 * 자기만의 공개 엔드포인트를 configurePublicEndpoints 에서 선언한다.
 *
 * 이 클래스 자체에는 @Configuration 을 붙이지 않는다. 붙이면 common 을 의존하는
 * 모든 서비스가 같은 규칙을 자동으로 물려받아, 공개 경로를 바꾸려면 common 을
 * 고쳐야 한다. 상속으로 두면 "무엇을 열지"는 각 서비스가 명시적으로 정한다.
 *
 * JWT 검증을 게이트웨이가 아니라 각 서비스가 하는 이유:
 * 게이트웨이 모듈을 두면 홉이 하나 늘고 단일 장애점이 생긴다. Ingress 는 경로
 * 라우팅만 하고, 토큰 검증은 서명만 확인하면 되므로 DB 조회도 필요 없다.
 */
public abstract class SecurityBaseConfig {

    /**
     * Actuator 전용 보안 규칙.
     *
     * 아래 apiFilterChain 은 마지막이 anyRequest().authenticated() 라서 actuator 도 401 로 막힌다.
     * Prometheus 는 JWT 를 갖고 있지 않으므로 수집이 불가능해진다.
     *
     * Spring Security 는 @Order 순서대로 검사해 처음 매칭되는 체인 하나만 적용하므로,
     * actuator 요청은 여기서 통과되고 일반 API 요청은 apiFilterChain(@Order 2) 으로 간다.
     *
     * permitAll 이지만 management.server.port 가 앱 포트와 분리돼 있고 그 포트를
     * 호스트/Ingress 에 게시하지 않으므로, 클러스터 안에서만 접근할 수 있다.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(EndpointRequest.toAnyEndpoint())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(
            HttpSecurity http,
            JwtAuthFilter jwtAuthFilter,
            /*
             * @Qualifier 가 반드시 필요하다.
             *
             * CorsConfigurationSource 를 타입만으로 주입받으면 후보가 둘이다.
             *   - 아래 우리가 만든 corsConfigurationSource
             *   - Spring MVC 가 자동 등록하는 mvcHandlerMappingIntrospector
             *     (이 클래스도 CorsConfigurationSource 를 구현한다)
             * 그래서 부팅이 NoUniqueBeanDefinitionException 으로 죽는다.
             *
             * 모놀리식일 때는 이 문제가 없었다. SecurityConfig 안에서
             * corsConfigurationSource() 를 메서드로 직접 불렀기 때문이다.
             * 공통 설정을 부모 클래스로 빼면서 빈 주입으로 바뀌었고,
             * 그 순간 타입 모호성이 드러났다.
             */
            @Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfigurationSource)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                /*
                 * 인증 정보가 없는 요청에 401 을 반환한다.
                 *
                 * 기본값이 403 인 것이 문제였다. JwtAuthFilter 는 토큰이 유효하지 않으면
                 * (만료 포함) SecurityContext 를 비워둔 채 그냥 통과시키고, 그 요청이
                 * anyRequest().authenticated() 에 걸린다. 이때 Spring Security 는
                 * httpBasic / formLogin 이 없으면 Http403ForbiddenEntryPoint 를 써서 403 을 낸다.
                 *
                 * 그런데 프론트(api/client.js)의 인터셉터는 401 만 처리한다.
                 *   → 403 은 아무도 안 잡는다 → 토큰을 지우지도, /login 으로 보내지도 않는다
                 *   → 사용자는 죽은 토큰을 들고 "아무것도 안 되는데 로그아웃도 안 되는" 상태가 된다
                 *
                 * 401(인증 안 됨)과 403(권한 없음)은 원래 의미가 다르다. 여기서 잡는 것은
                 * 전자뿐이다. 로그인은 했는데 권한이 없는 경우는 SecurityException →
                 * GlobalExceptionHandler 경로라 403 그대로 유지된다.
                 */
                .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"message\":\"로그인이 필요합니다.\"}");
                }))
                .authorizeHttpRequests(auth -> {
                    /*
                     * /error 는 반드시 permitAll 이어야 한다.
                     *
                     * sendError() 가 호출되면 서블릿 컨테이너가 /error 로 ERROR 디스패치를
                     * 건다. 여기가 authenticated() 로 남아 있으면 원래 상태 코드가 인증
                     * 실패로 덮인다. 실제로 겪은 사고: Riot 429 → sendError(429) → /error
                     * → 401 → 프론트 인터셉터가 로그아웃. 토큰은 멀쩡한데 로그인 화면으로 튕겼다.
                     */
                    auth.requestMatchers("/error").permitAll();
                    configurePublicEndpoints(auth);
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** 서비스별 공개(비로그인) 엔드포인트. 기본은 없음. */
    protected void configurePublicEndpoints(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors-allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
