package com.something.captor.repository;

import brave.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MapGenerator {

    @Autowired
    private final Tracer tracer;

    public Map<String, Object> generateFrom(final String body,
                                            final Map<String, Collection<String>> headers, // TODO If we do need headers consider changing the type to Map<String, Object> to accommodate the Rabbit message properties
                                            final String interactionName, final Type type) {

        // TODO Why is it necessary for the tests to pass?
        final String traceId = (tracer.currentSpan() == null) ? tracer.nextSpan().context().traceIdString() : tracer.currentSpan().context().traceIdString();

        return Map.of(
                "traceId", traceId,
                "type", type,
                "body", body,
                "headers", headers, // TODO Do we need to store this?
                "interactionName", interactionName
        );
    }
}