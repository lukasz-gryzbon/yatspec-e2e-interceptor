package com.yatspec.e2e.captor.rabbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yatspec.e2e.captor.http.ObjectMapperCreator;
import com.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.yatspec.e2e.captor.repository.MapGenerator;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.yatspec.e2e.captor.repository.Type.PUBLISH;

@Component
@RequiredArgsConstructor
public class PublishCaptor {

    private final ObjectMapper objectMapper = new ObjectMapperCreator().getObjectMapper().enable(INDENT_OUTPUT);

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final MapGenerator mapGenerator;

    public void capturePublishInteraction(final Message message) throws JsonProcessingException {
        final Map<String, Collection<String>> headers = message.getMessageProperties().getHeaders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> List.of(e.getValue().toString())));
        final Map<String, Object> map = mapGenerator.generateFrom(new String(message.getBody()), headers , "publish event from App to Exchange", PUBLISH);
        final Document document = Document.parse(objectMapper.writeValueAsString(map));
        interceptedDocumentRepository.save(document);
    }
}