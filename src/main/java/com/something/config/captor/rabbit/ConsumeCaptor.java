package com.something.config.captor.rabbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.something.config.captor.http.ObjectMapperCreator;
import com.something.config.captor.repository.InterceptedDocumentRepository;
import com.something.config.captor.repository.MapGenerator;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.something.config.captor.repository.Type.CONSUME;

@Component
@RequiredArgsConstructor
public class ConsumeCaptor {

    private final ObjectMapper objectMapper = new ObjectMapperCreator().getObjectMapper().enable(INDENT_OUTPUT);

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final MapGenerator mapGenerator;

    public void captureConsumeInteraction(final Message message) throws JsonProcessingException {
        final Map<String, Collection<String>> headers = message.getMessageProperties().getHeaders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> List.of(e.getValue().toString())));
        final Map<String, Object> map = mapGenerator.generateFrom(new String(message.getBody()), headers, "consume message from Exchange to App", CONSUME);
        final Document document = Document.parse(objectMapper.writeValueAsString(map));
        interceptedDocumentRepository.save(document);
    }
}