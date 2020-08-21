package com.something.config.captor.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nickmcdowall.lsd.http.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.http.naming.RegexResolvingNameMapper;
import com.nickmcdowall.lsd.http.naming.SourceNameMappings;
import com.something.config.captor.repository.InterceptedDocumentRepository;
import com.something.config.captor.repository.MapGenerator;
import feign.Response;
import feign.Util;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.nickmcdowall.lsd.http.common.HttpInteractionMessageTemplates.responseOf;
import static com.something.config.captor.repository.Type.RESPONSE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Component
@RequiredArgsConstructor
public class ResponseCaptor extends PathDerivingCaptor {

    private final SourceNameMappings sourceNames = SourceNameMappings.ALWAYS_APP;
    private final DestinationNameMappings destinationNames = new RegexResolvingNameMapper();
    private final ObjectMapper objectMapper = new ObjectMapperCreator().getObjectMapper().enable(INDENT_OUTPUT);

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final MapGenerator mapGenerator;

    @SneakyThrows
    public Map<String, Object> captureResponseInteraction(final Response response) {
        final String path = derivePath(response.request().url());
        final String source = sourceNames.mapForPath(path);
        final String destination = destinationNames.mapForPath(path);
        final String interactionMessage = responseOf(deriveStatus(response.status()), destination, source);
        final Map<String, Object> data = mapGenerator.generateFrom(extractResponseBodyToString(response), response.headers(), interactionMessage, RESPONSE);
        interceptedDocumentRepository.save(Document.parse(objectMapper.writeValueAsString(data)));
        return data;
    }

    public void captureResponseInteraction(final ClientHttpResponse response, final String path) throws IOException {
        final String body = copyBodyToString(response);
        final String source = sourceNames.mapForPath(path);
        final String destination = destinationNames.mapForPath(path);
        final String interactionMessage = responseOf(response.getStatusCode().toString(), destination, source);
        final var headers = response.getHeaders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (Collection<String>) e.getValue()));
        final Map<String, Object> data = mapGenerator.generateFrom(body, headers, interactionMessage, RESPONSE);
        interceptedDocumentRepository.save(Document.parse(objectMapper.writeValueAsString(data)));
    }


    private String extractResponseBodyToString(final Response response) throws IOException {
        final byte[] bytes = Util.toByteArray(response.body().asInputStream());
        return new String(bytes);
    }

    private String deriveStatus(final int code) {
        final Optional<HttpStatus> httpStatus = Optional.ofNullable(HttpStatus.resolve(code));
        return httpStatus.map(HttpStatus::toString)
                .orElse(String.format("<unresolved status:%s>", code));
    }

    private String copyBodyToString(final ClientHttpResponse response) throws IOException {
        if (response.getHeaders().getContentLength() == 0) {
            return EMPTY;
        }
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final InputStream inputStream = response.getBody();
        inputStream.transferTo(outputStream);
        return outputStream.toString();
    }
}