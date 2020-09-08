package com.yatspec.e2e.captor.repository;

import brave.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class TraceIdRetriever {

    @Autowired
    private final Tracer tracer;

    public String getTraceId(final Map<String, Collection<String>> headers) {
        log.info("headers received={}", headers);
        final String traceId;
        final Collection<String> b3Header = headers.get("b3");
        final Collection<String> xRequestInfo = headers.get("X-Request-Info");
        if (b3Header != null && b3Header.size() > 0) {
            traceId = getTraceIdFromB3Header(b3Header);
        } else if (xRequestInfo != null && xRequestInfo.size() > 0) {
            traceId = getTraceIdFromXRequestInfo(xRequestInfo);
        } else {
            traceId = getTraceIdFromTracer();
        }
        log.info("traceId retrieved={}", traceId);
        return traceId;
    }

    private String getTraceIdFromTracer() {
        final String traceId;
        log.info("in tracer");
        traceId = (tracer.currentSpan() == null) ? tracer.nextSpan().context().traceIdString() : tracer.currentSpan().context().traceIdString();
        return traceId;
    }

    private String getTraceIdFromXRequestInfo(final Collection<String> xRequestInfo) {
        final String traceId;
        log.info("in xRequestInfo");
        final String[] split = xRequestInfo.stream().findFirst().get().split(";");
        log.info("split:{}", split);
        final String referenceId = Arrays.stream(split).map(String::trim).filter(x -> x.startsWith("referenceId")).findFirst().get();
        log.info("referenceId:{}", referenceId);
        traceId = referenceId.split("=")[1];
        log.info("traceId:{}", traceId);
        return traceId;
    }

    private String getTraceIdFromB3Header(final Collection<String> b3Header) {
        log.info("in b3Header");
        return Arrays.stream(b3Header.stream().findFirst().get().split("-")).findFirst().get();
    }
}
