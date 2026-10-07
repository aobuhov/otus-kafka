package ru.otus.kafka.obukhov.term.order.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreatedEvent {

    private UUID orderId;
    private UUID customerId;
    private UUID restaurantId;
    private UUID eventId;
    private BigDecimal totalAmount;
    private OffsetDateTime createdAt;
}