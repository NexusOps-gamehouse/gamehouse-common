package gg.duo.common.event;

/**
 * 파티 채팅방이 열렸다. post 가 받아 posts.chat_room_id 를 채운다.
 *
 * post 가 chat 테이블을 조회하지 않고도 "이 글의 방 번호"를 알 수 있게 하는
 * 유일한 통로다. 이 값을 복제해 두지 않으면 목록 조회 한 번에 chat 서비스로
 * HTTP 호출이 글 개수만큼 나간다.
 */
public record ChatRoomCreatedEvent(Long roomId, Long postId, Long ownerId)
        implements DomainEvent {}
