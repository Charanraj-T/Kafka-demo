package com.ecommerce.project_injection_service.config;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

@Slf4j
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic productInTopic(@Value("${app.kafka.topics.in:products.in}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic productDlqTopic(@Value("${app.kafka.topics.dlq:products.dlq}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties properties) {
        return new DefaultKafkaProducerFactory<>(
                properties.buildProducerProperties(),
                new StringSerializer(),
                new DelegatingByTypeSerializer(Map.of(
                        byte[].class, new ByteArraySerializer(),
                        Object.class, new JacksonJsonSerializer<>()
                ), true)
        );
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public CommonErrorHandler errorHandler(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.dlq:products.dlq}") String dlqTopic,
            @Value("${app.kafka.retry.max-attempts:3}") long retryCount,
            @Value("${app.kafka.retry.backoff-ms:1000}") long backoffIntervalMs) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(dlqTopic, record.partition())
        );

        FixedBackOff backOff = new FixedBackOff(backoffIntervalMs, retryCount);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        errorHandler.addNotRetryableExceptions(
                ValidationException.class,
                MethodArgumentNotValidException.class,
                IllegalArgumentException.class
        );

        errorHandler.setRetryListeners((record, exception, deliveryAttempt) ->
                log.warn("[RETRY ATTEMPT {}] Failed for key={}: {}", deliveryAttempt, record.key(), exception.getMessage())
        );

        return errorHandler;
    }
}
