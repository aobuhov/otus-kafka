package ru.otus.kafka.obukhov.term.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.kafka.obukhov.term.payment.entity.CancelledOrder;

import java.util.UUID;

public interface CancelledOrderRepository extends JpaRepository<CancelledOrder, UUID> {
    boolean existsByOrderId(UUID orderId);
}