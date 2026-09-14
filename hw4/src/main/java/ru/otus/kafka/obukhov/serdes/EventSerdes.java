package ru.otus.kafka.obukhov.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import ru.otus.kafka.obukhov.models.Event;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class EventSerdes implements Serde<Event> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Serializer<Event> serializer() {
        return new Serializer<Event>() {
            @Override
            public byte[] serialize(String topic, Event data) {
                try {
                    return objectMapper.writeValueAsBytes(data);
                } catch (IOException e) {
                    throw new RuntimeException("Error serializing event", e);
                }
            }
        };
    }

    @Override
    public Deserializer<Event> deserializer() {
        return new Deserializer<Event>() {
            @Override
            public Event deserialize(String topic, byte[] data) {
                if (data == null || data.length == 0) {
                    return null;
                }
                try {
                    return objectMapper.readValue(data, Event.class);
                } catch (IOException e) {
                    throw new RuntimeException("Error deserializing event", e);
                }
            }
        };
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public void close() {
    }
}