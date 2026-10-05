package ru.otus.kafka.obukhov.term.audit;

import lombok.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

    private String traceId;
    private String userId;
    private String serviceName;
    private String endpoint;
    private String method;
    private int status;
    private long durationMs;
    private OffsetDateTime timestamp;
    private String clientIp;

}