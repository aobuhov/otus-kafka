package ru.otus.kafka.obukhov.term.payment.event;

import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderCancelledEvent {
    private UUID orderId;
}