package ru.otus.kafka.obukhov.term.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedEvent {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @CreationTimestamp
    @Column(name = "processed_at", updatable = false)
    private OffsetDateTime processedAt;
}