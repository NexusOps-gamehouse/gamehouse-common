package gg.duo.common.event;

/**
 * 누군가에게 알림을 보내야 한다.
 *
 * 알림 테이블은 user 서비스가 소유한다. post·chat 은 notifications 에 직접
 * INSERT 할 수 없으므로 이 이벤트를 던지고, user 의 소비자가 저장한다.
 */
public record NotificationRequestedEvent(Long userId, String message, String link)
        implements DomainEvent {}
