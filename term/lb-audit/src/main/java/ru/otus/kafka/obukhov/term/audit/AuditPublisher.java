package ru.otus.kafka.obukhov.term.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@RequiredArgsConstructor
public class AuditPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AuditProperties properties;

    public void publish(AuditEvent event) {
        try {
            // Ключ = traceId, чтобы события одного запроса шли в одну партицию
            kafkaTemplate.send(properties.getTopic(), event.getTraceId(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish audit event for traceId={}",
                                    event.getTraceId(), ex);
                        }
                    });
        } catch (Exception e) {
            // Аудит не должен ломать основной запрос
            log.error("Failed to send audit event", e);
        }
    }
}