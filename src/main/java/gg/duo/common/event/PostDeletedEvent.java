package gg.duo.common.event;

/** 모집글이 지워졌다. chat 이 받아 딸린 채팅방·메시지를 정리한다. */
public record PostDeletedEvent(Long postId) implements DomainEvent {}
