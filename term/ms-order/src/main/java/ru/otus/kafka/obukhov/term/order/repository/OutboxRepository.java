package ru.otus.kafka.obukhov.term.order.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.otus.kafka.obukhov.term.order.entity.OutboxEvent;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository  extends JpaRepository<OutboxEvent, UUID> {

    @Query(value = """
        SELECT * FROM outbox_event
        WHERE status = 'NEW'
        ORDER BY created_at ASC
        LIMIT :batchSize
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<OutboxEvent> lockBatchForProcessing(@Param("batchSize") int batchSize);
}
