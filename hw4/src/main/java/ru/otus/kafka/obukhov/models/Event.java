package ru.otus.kafka.obukhov.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class Event {
    @JsonProperty("id")
    private String id;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("data")
    private String data;

    public Event() {
        // Конструктор по умолчанию для Jackson
    }

    public Event(String id, String data) {
        this.id = id;
        this.data = data;
        this.timestamp = Instant.now().toEpochMilli();
    }

    public Event(String id, long timestamp, String data) {
        this.id = id;
        this.timestamp = timestamp;
        this.data = data;
    }

    @Override
    public String toString() {
        return String.format("Event{id='%s', timestamp=%d, data='%s'}",
                id, timestamp, data);
    }
}