package gg.duo.common.event;

/**
 * 모집글의 제목·상태가 바뀌었다.
 *
 * chat 이 chat_rooms 에 복제해 둔 제목/상태 스냅샷을 갱신한다.
 * 채팅방 화면 상단이 "이 방이 어느 글의 방이고 아직 모집 중인지"를 보여주는데,
 * 그 값을 매번 post 에 물으면 방을 열 때마다 HTTP 왕복이 한 번 더 생긴다.
 */
public record PostUpdatedEvent(Long postId, String title, String status)
        implements DomainEvent {}
