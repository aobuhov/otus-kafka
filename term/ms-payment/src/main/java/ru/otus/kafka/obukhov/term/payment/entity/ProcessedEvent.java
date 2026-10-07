package ru.otus.kafka.obukhov.term.payment.entity;

import jakarta.persistence.*;
import lombok.*;

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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
}





