package com.yatspec.e2e.captor.repository;

import brave.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MapGenerator {

    @Autowired
    private final Tracer tracer;

    public Map<String, Object> generateFrom(final String body,
                                            final Map<String, Collection<String>> headers,
                                            final String interactionName, final Type type) {

        log.info("YATSPEC-E2E: headers={}", headers);
        log.info("YATSPEC-E2E: tracer.currentSpan()={}", tracer.currentSpan());

        final String traceId;
        final Collection<String> b3Header = headers.get("b3");
        final Collection<String> xRequestInfo = headers.get("X-Request-Info");
        if (b3Header != null && b3Header.size() > 0) {
            log.info("in b3Header");
            traceId = Arrays.stream(b3Header.stream().findFirst().get().split("-")).findFirst().get();
        } else if (xRequestInfo != null && xRequestInfo.size()>0 ){
            log.info("in xRequestInfo");
            final String[] split = xRequestInfo.stream().findFirst().get().split(";");
            log.info("split:{}", split);
            final String referenceId = Arrays.stream(split).map(String::trim).filter(x -> x.startsWith("referenceId")).findFirst().get();
            log.info("referenceId:{}", referenceId);
            traceId = referenceId.split("=")[1];
            log.info("traceId:{}", traceId);
        } else {
            log.info("in tracer");
            traceId = (tracer.currentSpan() == null) ? tracer.nextSpan().context().traceIdString() : tracer.currentSpan().context().traceIdString();
        }
        log.info("YATSPEC-E2E: traceId={}", traceId);

        return Map.of(
                "traceId", traceId,
                "type", type,
                "body", body,
                "headers", headers, // TODO Do we need to store this?
                "interactionName", interactionName
        );
    }
}