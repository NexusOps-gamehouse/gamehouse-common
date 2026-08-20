package gg.duo.common.event;

/**
 * 방장이 채팅방에서 멤버를 내보냈다.
 *
 * post 가 받아 해당 참가 신청을 거절 처리한다. 예전에는 ChatService 가
 * ApplicationRepository 를 직접 잡고 상태를 바꿨다 — chat → post 방향의
 * 쓰기 의존이었고, post → chat 과 합쳐져 순환을 만들던 축 중 하나다.
 */
public record ChatMemberKickedEvent(Long roomId, Long postId, Long targetUserId)
        implements DomainEvent {}
