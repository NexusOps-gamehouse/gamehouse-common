package gg.duo.common.event;

/** 방장이 파티원을 확정했다. chat 이 받아 멤버의 confirmed 플래그를 올린다. */
public record ApplicationConfirmedEvent(Long applicationId, Long postId, String postTitle,
                                        Long postAuthorId, Long applicantId)
        implements DomainEvent {}
