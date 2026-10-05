package ru.otus.kafka.obukhov.term.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.otus.kafka.obukhov.term.order.dto.CreateOrderRequest;
import ru.otus.kafka.obukhov.term.order.dto.OrderResponse;
import ru.otus.kafka.obukhov.term.order.service.OrderService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }
}