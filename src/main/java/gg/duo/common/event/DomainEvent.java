package gg.duo.common.event;

/**
 * 서비스 경계를 넘는 사실(fact) 하나.
 *
 * 이름은 항상 과거형이다. "무엇을 해라"가 아니라 "무슨 일이 일어났다"를 알린다.
 * 명령이면 보낸 쪽이 받는 쪽의 처리 방식을 알아야 하고, 그 순간 결합이 되살아난다.
 */
public interface DomainEvent {
}
