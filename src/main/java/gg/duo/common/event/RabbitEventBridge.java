package gg.duo.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 브로커에서 받은 이벤트를 프로세스 안에 다시 뿌리는 다리.
 *
 * [왜 이 다리를 두는가]
 * 소비자들(PostEventConsumer, NotificationRequestedConsumer …)은 전부
 * @EventListener 로 쓰여 있다. 그것들을 전부 @RabbitListener 로 고치면
 *   - 소비자마다 큐·바인딩·에러 처리를 각각 신경 써야 하고
 *   - 나중에 브로커를 바꾸면 소비자를 또 전부 고쳐야 한다
 * 여기 한 곳에서 받아 로컬로 재발행하면, 소비자 코드는 브로커의 존재 자체를
 * 모른 채로 남는다. 실제로 이 변경에서 소비자는 한 줄도 바뀌지 않았다.
 *
 * [수동 변환을 쓰는 이유]
 * 메서드 파라미터를 DomainEvent 로 두면 리스너 어댑터가 파라미터 타입으로
 * 역직렬화를 시도할 수 있다. DomainEvent 는 인터페이스라 그 경로에서 실패한다.
 * 원본 Message 를 받아 TypeId 헤더 기준으로 직접 변환하면 그 모호함이 없다.
 *
 * [예외를 삼키는 이유]
 * 여기서 예외를 던지면 메시지가 큐로 되돌아가 무한 재시도가 된다. 형태가 깨진
 * 메시지 하나가 뒤에 줄 선 정상 메시지를 전부 막는다. 로그만 남기고 넘긴다.
 * (재시도가 필요한 실패는 dead-letter 큐로 다루는 게 맞고, 3단계 과제다)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "gamehouse.events.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitEventBridge {

    private final ApplicationEventPublisher localPublisher;
    private final MessageConverter domainEventMessageConverter;

    @RabbitListener(queues = "#{domainEventQueue.name}")
    public void onMessage(Message message) {
        try {
            Object payload = domainEventMessageConverter.fromMessage(message);
            if (payload instanceof DomainEvent event) {
                log.debug("이벤트 수신 {}", event.getClass().getSimpleName());
                localPublisher.publishEvent(event);
            } else {
                log.warn("이벤트가 아닌 메시지를 받았다. 버린다. type={}",
                        payload == null ? "null" : payload.getClass().getName());
            }
        } catch (Exception e) {
            log.error("이벤트 처리 실패. 메시지를 버린다. headers={}",
                    message.getMessageProperties().getHeaders(), e);
        }
    }
}
