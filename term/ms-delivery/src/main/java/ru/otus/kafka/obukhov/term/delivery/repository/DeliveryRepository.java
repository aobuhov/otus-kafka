package ru.otus.kafka.obukhov.term.delivery.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.kafka.obukhov.term.delivery.entity.Delivery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    Optional<Delivery> findByOrderId(UUID orderId);
    boolean existsByOrderId(UUID orderId);
    List<Delivery> findByEmployeeId(UUID employeeId);
}