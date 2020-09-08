package com.yatspec.e2e.config;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.yatspec.e2e.captor.http.RequestCaptor;
import com.yatspec.e2e.captor.http.ResponseCaptor;
import com.yatspec.e2e.interceptor.CustomRestTemplateCustomizer;
import com.yatspec.e2e.interceptor.LsdFeignLoggerInterceptor;
import com.yatspec.e2e.interceptor.LsdRestTemplateInterceptor;
import feign.Logger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;

/*
    All that should be required to enable capturing and saving interactions is:
    - import this library
    - declare a property with the db connection string
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "yatspec.lsd.db.connectionstring")
@RequiredArgsConstructor
public class HttpInterceptorConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public TestState testState() {
        return new TestState();
    }

    @Bean
    public LsdFeignLoggerInterceptor lsdFeignLoggerInterceptor(final RequestCaptor requestCaptor, final ResponseCaptor responseCaptor) {
        return new LsdFeignLoggerInterceptor(requestCaptor, responseCaptor);
    }

    @Bean
    public ClientHttpRequestInterceptor restTemplateInterceptor(final RequestCaptor requestCaptor, final ResponseCaptor responseCaptor) {
        return new LsdRestTemplateInterceptor(requestCaptor, responseCaptor);
    }

    @Bean
    public CustomRestTemplateCustomizer restTemplateCustomizer(final ClientHttpRequestInterceptor lsdRestTemplateInterceptor) {
        return new CustomRestTemplateCustomizer(lsdRestTemplateInterceptor);
    }
}