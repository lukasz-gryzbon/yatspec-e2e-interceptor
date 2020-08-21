package com.something.captor.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nickmcdowall.lsd.http.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.http.naming.RegexResolvingNameMapper;
import com.nickmcdowall.lsd.http.naming.SourceNameMappings;
import com.something.captor.repository.InterceptedDocumentRepository;
import com.something.captor.repository.MapGenerator;
import feign.Request;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bson.Document;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.nickmcdowall.lsd.http.common.HttpInteractionMessageTemplates.requestOf;
import static com.something.captor.repository.Type.REQUEST;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Component
@RequiredArgsConstructor
public class RequestCaptor extends PathDerivingCaptor {

    private final SourceNameMappings sourceNames = SourceNameMappings.ALWAYS_APP;
    private final DestinationNameMappings destinationNames = new RegexResolvingNameMapper();
    private final ObjectMapper objectMapper = new ObjectMapperCreator().getObjectMapper().enable(INDENT_OUTPUT);

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final MapGenerator mapGenerator;

    @SneakyThrows
    public void captureRequestInteraction(final Request request) {
        final Optional<byte[]> bodyData = Optional.ofNullable(request.body());
        final String body = bodyData.map(String::new).orElse(EMPTY);
        final String path = derivePath(request.url());
        final String source = sourceNames.mapForPath(path);
        final String destination = destinationNames.mapForPath(path);
        final String interactionMessage = requestOf(request.httpMethod().name(), path, source, destination);
        final Map<String, Object> map = mapGenerator.generateFrom(body, request.headers(), interactionMessage, REQUEST);
        final Document document = Document.parse(objectMapper.writeValueAsString(map));
        interceptedDocumentRepository.save(document);
    }

    public void captureRequestInteraction(final HttpRequest request, final String body) throws JsonProcessingException {
        final String path = request.getURI().getPath();
        final String source = sourceNames.mapForPath(path);
        final String destination = destinationNames.mapForPath(path);
        final String interactionMessage = requestOf(request.getMethodValue(), path, source, destination);
        final var headers = request.getHeaders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (Collection<String>) e.getValue()));
        final Map<String, Object> map = mapGenerator.generateFrom(body, headers, interactionMessage, REQUEST);
        final Document document = Document.parse(objectMapper.writeValueAsString(map));
        interceptedDocumentRepository.save(document);
    }
}