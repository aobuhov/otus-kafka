package ru.otus.kafka.obukhov.term.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDishRequest {

    @NotNull(message = "cnt is required")
    private Integer cnt;

    @NotNull(message = "price is required")
    private BigDecimal price;

    @NotNull(message = "dish is required")
    private UUID dishId;

}
