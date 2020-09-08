package com.yatspec.e2e.config;

import brave.Tracer;
import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.yatspec.e2e.captor.http.RequestCaptor;
import com.yatspec.e2e.captor.http.ResponseCaptor;
import com.yatspec.e2e.captor.name.ExchangeNameDeriver;
import com.yatspec.e2e.captor.name.ServiceNameDeriver;
import com.yatspec.e2e.captor.rabbit.ConsumeCaptor;
import com.yatspec.e2e.captor.rabbit.PublishCaptor;
import com.yatspec.e2e.captor.rabbit.header.HeaderRetriever;
import com.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.yatspec.e2e.captor.repository.MapGenerator;
import com.yatspec.e2e.captor.repository.TraceIdRetriever;
import com.yatspec.e2e.diagram.TestStateCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "yatspec.lsd.db.connectionstring")
@RequiredArgsConstructor
public class ServiceConfig {

    @Value("${info.app.name}")
    private String appName;

    @Value("${yatspec.lsd.db.connectionstring}")
    private String dbConnectionString;

    @Autowired
    private final Tracer tracer;

    @Autowired
    private TestState testState;

    @Bean
    public ServiceNameDeriver serviceNameDeriver() {
        return new ServiceNameDeriver(appName);
    }

    @Bean
    public ExchangeNameDeriver exchangeNameDeriver() {
        return new ExchangeNameDeriver();
    }

    @Bean
    public HeaderRetriever headerRetriever() {
        return new HeaderRetriever();
    }
    @Bean
    public TraceIdRetriever traceIdRetriever() {
        return new TraceIdRetriever(tracer);
    }

    @Bean
    public RequestCaptor requestCaptor(final InterceptedDocumentRepository interceptedDocumentRepository,
                                       final MapGenerator mapGenerator,
                                       final ServiceNameDeriver serviceNameDeriver) {

        return new RequestCaptor(interceptedDocumentRepository, mapGenerator, serviceNameDeriver);
    }

    @Bean
    public ResponseCaptor responseCaptor(final InterceptedDocumentRepository interceptedDocumentRepository,
                                         final MapGenerator mapGenerator,
                                         final ServiceNameDeriver serviceNameDeriver) {

        return new ResponseCaptor(interceptedDocumentRepository, mapGenerator, serviceNameDeriver);
    }

    @Bean
    public MapGenerator mapGenerator(final TraceIdRetriever traceIdRetriever) {
        return new MapGenerator(traceIdRetriever);
    }

    @Bean
    public ConsumeCaptor consumeCaptor(final InterceptedDocumentRepository interceptedDocumentRepository,
                                       final MapGenerator mapGenerator,
                                       final ServiceNameDeriver serviceNameDeriver,
                                       final ExchangeNameDeriver exchangeNameDeriver,
                                       final HeaderRetriever headerRetriever) {
        return new ConsumeCaptor(interceptedDocumentRepository, mapGenerator, serviceNameDeriver, exchangeNameDeriver, headerRetriever);
    }

    @Bean
    public PublishCaptor publishCaptor(final InterceptedDocumentRepository interceptedDocumentRepository,
                                       final MapGenerator mapGenerator,
                                       final ServiceNameDeriver serviceNameDeriver,
                                       final ExchangeNameDeriver exchangeNameDeriver,
                                       final HeaderRetriever headerRetriever) {
        return new PublishCaptor(interceptedDocumentRepository, mapGenerator, serviceNameDeriver, exchangeNameDeriver, headerRetriever);
    }

    @Bean
    public InterceptedDocumentRepository interceptedDocumentRepository() {
        return new InterceptedDocumentRepository(dbConnectionString);
    }

    @Bean
    public TestStateCollector testStateCollector() {
        return new TestStateCollector(dbConnectionString, testState);
    }
}
