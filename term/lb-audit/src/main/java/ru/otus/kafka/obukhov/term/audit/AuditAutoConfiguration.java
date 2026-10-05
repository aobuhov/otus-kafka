package ru.otus.kafka.obukhov.term.audit;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
@ConditionalOnClass({KafkaTemplate.class, jakarta.servlet.Filter.class})
@ConditionalOnProperty(prefix = "audit", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AuditProperties.class)
public class AuditAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AuditPublisher auditPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                         AuditProperties properties) {
        return new AuditPublisher(kafkaTemplate, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditFilter auditFilter(AuditPublisher publisher, AuditProperties properties) {
        return new AuditFilter(publisher, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public AuditWebFilter auditWebFilter(AuditPublisher publisher, AuditProperties properties) {
        return new AuditWebFilter(publisher, properties);
    }
}