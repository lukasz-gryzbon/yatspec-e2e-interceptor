package com.something.config.captor.repository;

import brave.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MapGenerator {

    private final Tracer tracer;

    public Map<String, Object> generateFrom(final String body,
                                            final Map<String, Collection<String>> headers, // TODO If we do need headers consider changing the type to Map<String, Object> to accommodate the Rabbit message properties
                                            final String interactionName, final Type type) {
        return  Map.of(
                "traceId", tracer.currentSpan().context().traceIdString(),
                "type", type,
                "body", body,
                "headers", headers, // TODO Do we need to store this?
                "interactionName", interactionName
        );
    }
}