package gg.duo.common.event;

/**
 * 이벤트 발행 창구.
 *
 * 발행하는 쪽은 "누가 배달하는지"를 몰라야 한다. 지금은 프로세스 내
 * ApplicationEvent 로 배달하고(LocalDomainEventPublisher), 서비스가 각자
 * 파드로 나뉘는 시점에 RabbitMQ 구현으로 갈아끼운다. 이 인터페이스를 참조하는
 * publisher/consumer 코드는 그때 한 줄도 바뀌지 않는다.
 */
public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
