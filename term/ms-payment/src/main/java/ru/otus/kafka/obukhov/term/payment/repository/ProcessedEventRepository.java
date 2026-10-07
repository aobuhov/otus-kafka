package ru.otus.kafka.obukhov.term.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.otus.kafka.obukhov.term.payment.entity.ProcessedEvent;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

    @Modifying
    @Query(value = """
    INSERT INTO processed_events (event_id, event_type)
    VALUES (:eventId, :eventType)
    ON CONFLICT (event_id) DO NOTHING
    """, nativeQuery = true)
    int insertIfAbsent(@Param("eventId") UUID eventId, @Param("eventType") String eventType);

}
