package gg.duo.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * 모든 서비스가 공유하는 에러 응답 형태.
 *
 * 응답 본문은 항상 {"message": ...} 다. 프론트(api/client.js 의 errMsg)가 그 키만 읽는다.
 * 서비스가 나뉘어도 이 형태가 흔들리면 안 되므로 common 에 둔다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 도메인 규칙 위반 — ErrorCode 가 상태 코드를 정한다. */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, String>> business(BusinessException e) {
        return ResponseEntity.status(e.errorCode().status())
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> badRequest(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    /**
     * 예외 메시지가 있으면 그대로 내보낸다.
     *
     * 이전에는 무조건 "대상을 찾을 수 없습니다." 로 덮어써서, 던지는 쪽이 아무리
     * 구체적으로 써도 프론트에는 같은 문장만 도착했다.
     * (예: "소환사를 찾을 수 없습니다. 게임명과 태그를 확인해 주세요.")
     *
     * orElseThrow() 처럼 메시지 없이 던지는 자리도 많으므로 기본 문구는 남겨둔다.
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> notFound(NoSuchElementException e) {
        String message = (e.getMessage() != null && !e.getMessage().isBlank())
                ? e.getMessage()
                : "대상을 찾을 수 없습니다.";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", message));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> forbidden(SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
    }
}
