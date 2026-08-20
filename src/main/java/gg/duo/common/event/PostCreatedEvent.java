package gg.duo.common.event;

/** 모집글이 만들어졌다. chat 이 받아 파티 채팅방을 미리 연다. */
public record PostCreatedEvent(Long postId, Long authorId, String title, String status)
        implements DomainEvent {}
