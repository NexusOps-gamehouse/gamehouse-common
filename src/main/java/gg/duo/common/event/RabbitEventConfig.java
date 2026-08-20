package gg.duo.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 서비스 간 이벤트 배달 설정.
 *
 * [구조]
 *   발행자 ──▶ gamehouse.events (topic exchange) ──▶ 서비스별 큐 ──▶ 소비자
 *
 * exchange 하나에 서비스마다 자기 큐를 하나씩 달고, 라우팅 키는 '#'(전부)로 묶는다.
 * 즉 모든 서비스가 모든 이벤트를 받는다. 자기가 처리할 이벤트가 아니면
 * 해당 @EventListener 가 없어서 아무 일도 안 일어난다.
 *
 * 왜 이렇게 단순하게 두는가: 라우팅 키를 이벤트별로 세분하면 새 이벤트를 추가할
 * 때마다 바인딩을 함께 고쳐야 하고, 빠뜨리면 "발행은 되는데 아무도 못 받는" 상태가
 * 조용히 생긴다. 지금 트래픽 규모에서 전부 받는 비용은 무시할 수준이다.
 *
 * 큐 이름에 서비스명을 넣는 이유: 큐를 공유하면 이벤트 하나를 여러 서비스가
 * 나눠 갖는다(경쟁 소비). post 가 받아야 할 것을 chat 이 집어가면 조용히 유실된다.
 * 큐가 따로여야 각자 자기 사본을 받는다.
 */
@Configuration
@ConditionalOnProperty(name = "gamehouse.events.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitEventConfig {

    public static final String EXCHANGE = "gamehouse.events";

    /** 이벤트 계약이 사는 패키지. 여기 것만 역직렬화를 허용한다. */
    private static final String TRUSTED_PACKAGE = "gg.duo.common.event";

    @Bean
    public TopicExchange domainEventExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    /**
     * 이 서비스 전용 큐.
     *
     * durable: 브로커가 재시작해도 큐가 남는다.
     * 큐가 사라지면 그동안 쌓인 이벤트도 같이 사라진다 — 소비자가 잠깐 죽어 있는
     * 사이에 온 이벤트를 살려두는 게 브로커를 쓰는 이유의 절반이다.
     */
    @Bean
    public Queue domainEventQueue(@Value("${spring.application.name}") String applicationName) {
        return QueueBuilder.durable(EXCHANGE + "." + applicationName).build();
    }

    @Bean
    public Binding domainEventBinding(Queue domainEventQueue, TopicExchange domainEventExchange) {
        return BindingBuilder.bind(domainEventQueue).to(domainEventExchange).with("#");
    }

    /**
     * JSON 변환기.
     *
     * ObjectMapper 는 Spring Boot 가 구성해 둔 것을 그대로 쓴다. 새로 만들면
     * 날짜 모듈 등 앱 전역 설정이 빠져 HTTP 응답과 이벤트의 직렬화 규칙이 갈린다.
     *
     * TypeId 헤더: 보내는 쪽이 클래스 이름을 헤더에 실어 보내고 받는 쪽이 그걸로
     * 어떤 record 인지 판단한다. 발행자와 소비자가 같은 common 을 쓰므로 이름이 맞는다.
     * 신뢰 패키지를 제한해 임의 클래스가 역직렬화되지 않게 한다.
     */
    @Bean
    public MessageConverter domainEventMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages(TRUSTED_PACKAGE);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    /**
     * 기본 배달부. 프로세스 밖으로 나가는 유일한 경로다.
     *
     * transport 를 local 로 두면 이 빈이 뜨지 않고 LocalDomainEventPublisher 가
     * 대신 뜬다(단일 프로세스 테스트용).
     */
    @Bean
    @ConditionalOnProperty(name = "gamehouse.events.transport",
            havingValue = "rabbit", matchIfMissing = true)
    public DomainEventPublisher rabbitDomainEventPublisher(RabbitTemplate domainEventRabbitTemplate) {
        return new RabbitDomainEventPublisher(domainEventRabbitTemplate);
    }

    @Bean
    public RabbitTemplate domainEventRabbitTemplate(ConnectionFactory connectionFactory,
                                                    MessageConverter domainEventMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(domainEventMessageConverter);
        return template;
    }
}
