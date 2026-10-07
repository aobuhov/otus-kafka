package ru.otus.kafka.obukhov.term.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "cancelled_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelledOrder {
    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;
}
