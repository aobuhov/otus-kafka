package ru.otus.kafka.obukhov.term.order.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusEvent {
    private UUID orderId;
}