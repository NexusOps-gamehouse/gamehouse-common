package gg.duo.common.security;

/**
 * "이 사용자가 방금 활동했다"를 기록하는 창구.
 *
 * JwtAuthFilter 는 common 에 있고, common 에는 Repository 를 둘 수 없다.
 * 그런데 마지막 활동 시각(users.last_active_at)은 user 서비스가 소유한 컬럼이다.
 *
 * 그래서 필터는 이 인터페이스만 알고, 구현은 user 서비스가 제공한다.
 * post·chat·riot 에는 이 빈이 없으므로 아무 일도 일어나지 않는다 —
 * 남의 테이블을 건드릴 방법이 구조적으로 없다.
 */
public interface UserActivityRecorder {

    void recordActive(Long userId);
}
