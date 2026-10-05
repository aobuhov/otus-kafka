package ru.otus.kafka.obukhov.term.restaurant.client;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.otus.kafka.obukhov.term.restaurant.dto.OrderDetailsDto;

import java.util.UUID;

@Slf4j
@Component
public class OrderClient {

    private final RestClient restClient;

    public OrderClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://localhost:8081")
                .build();
    }

    public OrderDetailsDto getOrder(UUID orderId) {
        return restClient.get()
                .uri("/api/v1/orders/{id}", orderId)
                .retrieve()
                .body(OrderDetailsDto.class);
    }
}