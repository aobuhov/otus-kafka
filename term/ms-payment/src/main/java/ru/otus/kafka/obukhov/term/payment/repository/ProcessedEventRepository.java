package ru.otus.kafka.obukhov.term.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.kafka.obukhov.term.payment.entity.ProcessedEvent;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
}
