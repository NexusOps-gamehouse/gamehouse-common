package gg.duo.common.security;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 활동 시각 기록기 — user 서비스에만 존재한다.
     *
     * ObjectProvider 로 받는 이유: 이 필터는 네 서비스가 전부 쓰는데,
     * UserActivityRecorder 빈은 user 에만 있다. 생성자로 직접 받으면
     * post·chat·riot 이 부팅에서 NoSuchBeanDefinitionException 으로 죽는다.
     */
    private final ObjectProvider<UserActivityRecorder> activityRecorder;

    /**
     * 에러 디스패치에서도 이 필터를 실행한다. (기본값은 실행하지 않음)
     *
     * [왜 필요한가]
     * 컨트롤러에서 sendError() 가 호출되면 서블릿 컨테이너가 같은 요청을 /error 로
     * 한 번 더 돌린다(에러 디스패치). Spring Security 필터 체인은 그 2회차에도 실행되는데,
     * OncePerRequestFilter 는 shouldNotFilterErrorDispatch() 기본값이 true 라
     * 이 필터만 건너뛴다. SecurityContext 는 1회차가 끝나며 비워지므로,
     * 2회차는 Authorization 헤더를 들고도 "인증되지 않은 요청"이 된다.
     *
     * 그러면 /error 가 인증 요구에 걸리고, AuthenticationEntryPoint 가
     * 원래 상태 코드를 인증 실패 코드로 덮어쓴다. 실제로 겪은 사고:
     *   Riot 429 → ResponseStatusException → sendError(429) → /error
     *            → 인증 없음 → 401 → 프론트 인터셉터가 로그아웃
     * 토큰은 멀쩡한데 레이트 리밋 안내 대신 로그인 화면으로 튕겼다.
     */
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtTokenProvider.validate(token)) {
                Long userId = jwtTokenProvider.getUserId(token);
                var auth = new UsernamePasswordAuthenticationToken(userId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);

                // 온라인 상태 표시용 (마지막 활동 시각 갱신).
                //
                // 에러 디스패치는 같은 요청의 2회차이므로 여기서는 건너뛴다.
                // 인증은 다시 세워야 하지만(SecurityContext 가 비어 있다),
                // 활동 시각은 1회차에서 이미 갱신했다.
                if (request.getDispatcherType() != DispatcherType.ERROR) {
                    activityRecorder.ifAvailable(r -> r.recordActive(userId));
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
