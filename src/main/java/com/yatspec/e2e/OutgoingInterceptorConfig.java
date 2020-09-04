package com.yatspec.e2e;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.yatspec.e2e.captor.http.RequestCaptor;
import com.yatspec.e2e.captor.http.ResponseCaptor;
import com.yatspec.e2e.interceptor.LsdFeignLoggerInterceptor;
import com.yatspec.e2e.interceptor.LsdRestTemplateCustomizer;
import com.yatspec.e2e.interceptor.LsdRestTemplateInterceptor;
import feign.Logger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;

/*
    All that should be required to enable capturing and saving interactions is:
    - import this library
    - declare a property with the db connection string
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "yatspec.lsd.db.connectionstring") // TODO Needs to be tested for missing value
@Slf4j
@ComponentScan({"com.yatspec.e2e"})
public class OutgoingInterceptorConfig {

    private final RequestCaptor requestCaptor;
    private final ResponseCaptor responseCaptor;

    @Bean
    public LsdFeignLoggerInterceptor lsdFeignLoggerInterceptor() {
        log.info("Creating bean:{}", LsdFeignLoggerInterceptor.class);
        return new LsdFeignLoggerInterceptor(requestCaptor, responseCaptor);
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public TestState testState() {
        return new TestState();
    }

    @Bean
    public ClientHttpRequestInterceptor lsdRestTemplateInterceptor(final RequestCaptor requestCaptor, final ResponseCaptor responseCaptor) {
        return new LsdRestTemplateInterceptor(requestCaptor, responseCaptor);
    }

    @Bean
    public LsdRestTemplateCustomizer lsdRestTemplateCustomizer(final ClientHttpRequestInterceptor lsdRestTemplateInterceptor) {
        return new LsdRestTemplateCustomizer(lsdRestTemplateInterceptor);
    }
}