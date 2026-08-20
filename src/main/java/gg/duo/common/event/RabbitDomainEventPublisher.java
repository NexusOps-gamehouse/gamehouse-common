package gg.duo.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * 브로커를 거치는 배달부. 서비스가 별도 프로세스로 나뉜 뒤의 기본 구현이다.
 *
 * 라우팅 키는 이벤트 클래스 이름을 쓴다. 지금은 모든 큐가 '#' 로 전부 받지만,
 * 나중에 특정 이벤트만 골라 받도록 좁힐 때 키가 이미 있어야 바인딩만 바꾸면 된다.
 *
 * 빈 등록은 RabbitEventConfig 가 한다(@Component 아님). 이벤트 기능을
 * 통째로 끄는 서비스(riot)에서 이 클래스만 혼자 살아남지 않게 하기 위해서다.
 */
@Slf4j
@RequiredArgsConstructor
public class RabbitDomainEventPublisher implements DomainEventPublisher {

    private final RabbitTemplate domainEventRabbitTemplate;

    @Override
    public void publish(DomainEvent event) {
        String routingKey = event.getClass().getSimpleName();
        log.debug("이벤트 발행 {} → {}", routingKey, RabbitEventConfig.EXCHANGE);
        domainEventRabbitTemplate.convertAndSend(RabbitEventConfig.EXCHANGE, routingKey, event);
    }
}
