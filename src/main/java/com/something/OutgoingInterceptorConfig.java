package com.something;

import com.something.captor.http.RequestCaptor;
import com.something.captor.http.ResponseCaptor;
import com.something.interceptor.LsdFeignLoggerInterceptor;
import feign.Logger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/*
    All that should be required to enable capturing and saving interactions is:
    - import a dependency with this config
    - declare a property with the db connection string
 */
// TODO Need to include RabbitTemplate interceptor
// TODO Should we include any other interceptors? eg. RestTemplate?
// TODO Extract to a library
@Configuration
@RequiredArgsConstructor
//@ConditionalOnProperty(name = "${yatspec.lsd.db.connectionstring}") // TODO Needs to be tested for missing value
@Slf4j
@ComponentScan({"com.something"})
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
}