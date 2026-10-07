package ru.otus.kafka.obukhov.term.delivery.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.kafka.obukhov.term.delivery.entity.*;
import ru.otus.kafka.obukhov.term.delivery.event.DeliveryEvent;
import ru.otus.kafka.obukhov.term.delivery.repository.*;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements ru.otus.kafka.obukhov.term.delivery.service.DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final EmployeeRepository employeeRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public Delivery assignEmployee(UUID deliveryId, UUID employeeId) {
        Delivery delivery = findDelivery(deliveryId);
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        delivery.setEmployee(employee);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        Delivery saved = deliveryRepository.save(delivery);

        publish(saved, "delivery.assigned");
        return saved;
    }

    @Transactional
    public Delivery markPickedUp(UUID deliveryId) {
        Delivery delivery = findDelivery(deliveryId);

        delivery.setStatus(DeliveryStatus.PICKED_UP);
        Delivery saved = deliveryRepository.save(delivery);

        publish(saved, "delivery.picked_up");
        return saved;
    }

    @Transactional
    public Delivery markCompleted(UUID deliveryId) {
        Delivery delivery = findDelivery(deliveryId);

        delivery.setStatus(DeliveryStatus.COMPLETED);
        Delivery saved = deliveryRepository.save(delivery);

        publish(saved, "delivery.completed");
        return saved;
    }

    private Delivery findDelivery(UUID id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + id));
    }

    private void publish(Delivery delivery, String topic) {
        DeliveryEvent event = DeliveryEvent.builder()
                .deliveryId(delivery.getId())
                .orderId(delivery.getOrderId())
                .employeeId(delivery.getEmployee() != null
                        ? delivery.getEmployee().getId() : null)
                .status(delivery.getStatus().name())
                .eventId(UUID.randomUUID())
                .build();

        kafkaTemplate.send(topic, delivery.getOrderId().toString(), event);
        log.info("Published {} for orderId={}", topic, delivery.getOrderId());
    }
}