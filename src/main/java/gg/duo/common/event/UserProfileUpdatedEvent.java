package gg.duo.common.event;

/**
 * 프로필이 바뀌었다. 닉네임·프로필 이미지를 복제해 둔 서비스가 받아 갱신한다.
 * (chat 의 메시지 발신자 스냅샷 등)
 */
public record UserProfileUpdatedEvent(Long userId, String nickname, String profileImageUrl)
        implements DomainEvent {}
