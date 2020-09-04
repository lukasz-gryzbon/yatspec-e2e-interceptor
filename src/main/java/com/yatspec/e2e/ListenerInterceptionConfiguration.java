package com.yatspec.e2e;

import com.yatspec.e2e.captor.rabbit.ConsumeCaptor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@ConditionalOnProperty(name = {"yatspec.lsd.db.connectionstring"})
@ConditionalOnBean(SimpleRabbitListenerContainerFactory.class)
@Configuration
@RequiredArgsConstructor
@Slf4j
/*
    This config add the interception of messages to RabbitMq listeners
 */
public class ListenerInterceptionConfiguration {

    private final SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory;
    private final ConsumeCaptor consumeCaptor;

    @PostConstruct
    public void postConstruct() {
        simpleRabbitListenerContainerFactory.setAfterReceivePostProcessors(this::postProcessMessage);
    }

    @SneakyThrows
    private Message postProcessMessage(final Message message) {
        consumeCaptor.captureConsumeInteraction(MessageBuilder.fromMessage(message).build());
        return message;
    }
}