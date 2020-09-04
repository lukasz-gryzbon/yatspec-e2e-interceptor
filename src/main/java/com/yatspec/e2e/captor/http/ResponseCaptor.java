package com.yatspec.e2e.captor.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nickmcdowall.lsd.http.naming.DestinationNameMappings;
import com.yatspec.e2e.captor.name.AppNameDeriver;
import com.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.yatspec.e2e.captor.repository.MapGenerator;
import feign.Response;
import feign.Util;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import static com.nickmcdowall.lsd.http.naming.UserSuppliedDestinationMappings.userSuppliedDestinationMappings;
import static com.yatspec.e2e.captor.repository.Type.RESPONSE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResponseCaptor extends PathDerivingCaptor {

    private final ObjectMapper objectMapper = new ObjectMapperCreator().getObjectMapper().enable(INDENT_OUTPUT);

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final MapGenerator mapGenerator;
    private final AppNameDeriver appNameDeriver;

    @SneakyThrows
    public Map<String, Object> captureResponseInteraction(final Response response) {
        try {
            final String path = derivePath(response.request().url());
            final String source = appNameDeriver.derive();
            final String destination = destinationNameMapping().mapForPath(path);
            log.info("YATSPEC-E2E: response capture - source:{}, destination:{}", source, destination);
            final String interactionMessage = responseOf(deriveStatus(response.status()), destination, source);
            final Map<String, Object> data = mapGenerator.generateFrom(extractResponseBodyToString(response), response.headers(), interactionMessage, RESPONSE);
            interceptedDocumentRepository.save(Document.parse(objectMapper.writeValueAsString(data)));
            return data;
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    // TODO This should be moved to the E2E tests
    private DestinationNameMappings destinationNameMapping() {
        return userSuppliedDestinationMappings(Map.of(
                "/consumer-orders", "ConsumerOrderService"
        ));
    }

    public void captureResponseInteraction(final ClientHttpResponse response, final String path) throws IOException {
        final String body = copyBodyToString(response);
        final String source = appNameDeriver.derive();
        final String destination = destinationNameMapping().mapForPath(path);
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