package ru.otus.kafka.obukhov.term.audit;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "audit")
public class AuditProperties {

    private boolean enabled = true;
    private String topic = "audit.events";
    private String serviceName; //Имя сервиса (подставляется автоматически из spring.application.name)
    private String[] excludePaths = {"/actuator/**", "/health", "/error"};  //Список путей, которые НЕ нужно аудировать (health, actuator и т.д.)

}