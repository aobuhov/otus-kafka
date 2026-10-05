package ru.otus.kafka.obukhov.term.restaurant.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {
    private UUID dishId;
    private Integer cnt;
    private BigDecimal price;
}