package gg.duo.common.event;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 프로세스 내 배달부. 기본값이 아니다.
 *
 * ⚠️ 이 구현은 같은 JVM 안에서만 도달한다. user/post/chat 이 각각 다른
 *    프로세스로 뜨는 지금 구성에서는 이벤트가 상대에게 닿지 않는다.
 *    실제로 이걸 기본으로 두었다가 "글을 써도 채팅방이 안 생기는" 문제를 겪었다.
 *    빌드도 통과하고 앱도 다 떴지만 서비스 간 소식만 조용히 사라졌다.
 *
 * 남겨두는 이유: 브로커 없이 단일 프로세스로 돌려보고 싶을 때(통합 테스트 등)
 * 쓸 데가 있다. 쓰려면 명시적으로 켜야 한다.
 *
 *   gamehouse:
 *     events:
 *       transport: local
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "gamehouse.events.transport", havingValue = "local")
public class LocalDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher delegate;

    @Override
    public void publish(DomainEvent event) {
        delegate.publishEvent(event);
    }
}
