package ru.otus.kafka.obukhov.term.order.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.kafka.obukhov.term.order.entity.EventStatus;
import ru.otus.kafka.obukhov.term.order.entity.OutboxEvent;
import ru.otus.kafka.obukhov.term.order.repository.OutboxRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final int BATCH_SIZE = 50;
    private static final int MAX_ATTEMPTS = 5;

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishOutboxEvents() {
        List<OutboxEvent> batch = outboxRepository.lockBatchForProcessing(BATCH_SIZE);
        if (batch.isEmpty()) {
            return;
        }

        log.debug("Publishing {} outbox events", batch.size());

        for (OutboxEvent event : batch) {
            try {
                // Синхронная отправка внутри транзакции — ждём подтверждения Kafka
                kafkaTemplate.send(
                        event.getTopic(),
                        event.getAggregateId().toString(),
                        event.getPayload()
                ).get(5, TimeUnit.SECONDS);

                event.setStatus(EventStatus.PUBLISHED);
                event.setPublishedAt(OffsetDateTime.now());
                event.setLastError(null);
                log.info("Published outbox event id={} type={} aggregateId={}",
                        event.getId(), event.getEventType(), event.getAggregateId());

            } catch (Exception e) {
                int attempts = event.getAttempts() + 1;
                event.setAttempts(attempts);
                event.setLastError(truncate(e.getMessage(), 1000));

                if (attempts >= MAX_ATTEMPTS) {
                    event.setStatus(EventStatus.FAILED);
                    log.error("Outbox event id={} failed after {} attempts, marking FAILED",
                            event.getId(), attempts, e);
                } else {
                    log.warn("Outbox event id={} attempt {} failed, will retry",
                            event.getId(), attempts, e);
                }
            }
        }

        outboxRepository.saveAll(batch);
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}