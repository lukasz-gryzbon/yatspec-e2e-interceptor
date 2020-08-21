package com.something.config.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.something.config.captor.rabbit.ConsumeCaptor;
import com.something.config.captor.rabbit.PublishCaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
@Profile({"local", "docker"})
@RequiredArgsConstructor
public class LsdRabbitTemplateInterceptor {

    private final RabbitTemplate rabbitTemplate;
    private final PublishCaptor publishCaptor;
    private final ConsumeCaptor consumeCaptor;

    // TODO How do we make it conditional?
    @PostConstruct
    public void configureRabbitTemplatePublishInterceptor() {
        rabbitTemplate.addBeforePublishPostProcessors(message -> {
            try {
                publishCaptor.capturePublishInteraction(MessageBuilder.fromMessage(message).build());
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
            }
            return message;
        });
        rabbitTemplate.addAfterReceivePostProcessors(message -> {
            try {
                consumeCaptor.captureConsumeInteraction(MessageBuilder.fromMessage(message).build());
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
            }
            return message;
        });
    }
}