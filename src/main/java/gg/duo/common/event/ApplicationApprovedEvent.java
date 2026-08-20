package gg.duo.common.event;

/**
 * 방장이 참가 신청을 승인했다. chat 이 받아 신청자를 채팅방 멤버로 넣는다.
 *
 * (설계서에는 없던 이벤트다. 기존 코드에서 채팅방 입장을 만드는 지점이
 *  confirm 이 아니라 approve 라서 둘을 나눠야 했다.)
 */
public record ApplicationApprovedEvent(Long applicationId, Long postId, String postTitle,
                                       Long postAuthorId, Long applicantId)
        implements DomainEvent {}
