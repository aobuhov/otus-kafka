package ru.otus.kafka.obukhov.term.delivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.otus.kafka.obukhov.term.delivery.entity.Delivery;
import ru.otus.kafka.obukhov.term.delivery.repository.DeliveryRepository;
import ru.otus.kafka.obukhov.term.delivery.service.DeliveryService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final DeliveryRepository deliveryRepository;

    @GetMapping
    public List<Delivery> getAll() {
        return deliveryRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Delivery> getById(@PathVariable UUID id) {
        return deliveryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Назначить курьера на доставку. NEW -> ASSIGNED */
    @PostMapping("/{deliveryId}/assign/{employeeId}")
    public ResponseEntity<Delivery> assign(@PathVariable UUID deliveryId,
                                           @PathVariable UUID employeeId) {
        return ResponseEntity.ok(deliveryService.assignEmployee(deliveryId, employeeId));
    }

    /** Курьер выехал. ASSIGNED -> PICKED_UP */
    @PostMapping("/{deliveryId}/pick-up")
    public ResponseEntity<Delivery> pickUp(@PathVariable UUID deliveryId) {
        return ResponseEntity.ok(deliveryService.markPickedUp(deliveryId));
    }

    /** Заказ доставлен. PICKED_UP -> COMPLETED */
    @PostMapping("/{deliveryId}/complete")
    public ResponseEntity<Delivery> complete(@PathVariable UUID deliveryId) {
        return ResponseEntity.ok(deliveryService.markCompleted(deliveryId));
    }
}