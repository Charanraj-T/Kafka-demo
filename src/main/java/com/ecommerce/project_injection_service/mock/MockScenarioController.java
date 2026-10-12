package com.ecommerce.project_injection_service.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/mock")
public class MockScenarioController {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.in:products.in}")
    private String inputTopic;

    public MockScenarioController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping("/publish")
    public ResponseEntity<Map<String, Object>> publishCustomMessage(@RequestBody Map<String, Object> payload) {
        String key = (String) payload.getOrDefault("sku", "default-key");
        log.info("[MOCK PUBLISH] Publishing custom message to topic '{}' with key '{}': {}", inputTopic, key, payload);
        kafkaTemplate.send(inputTopic, key, payload);

        return ResponseEntity.ok(Map.of(
                "status", "PUBLISHED",
                "topic", inputTopic,
                "key", key,
                "message", payload
        ));
    }

    @PostMapping("/scenario")
    public ResponseEntity<Map<String, Object>> runScenarioFromMockFile() throws IOException {
        ClassPathResource resource = new ClassPathResource("mock/mock-products.json");
        List<Map<String, Object>> productMessages = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<Map<String, Object>>>() {}
        );

        log.info("[MOCK SCENARIO] Loaded {} message(s) from mock/mock-products.json", productMessages.size());

        for (Map<String, Object> message : productMessages) {
            String key = (String) message.getOrDefault("sku", "default-key");
            kafkaTemplate.send(inputTopic, key, message);
            log.info("[MOCK SCENARIO] Sent message to topic '{}': SKU='{}'", inputTopic, key);
        }

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "totalMessagesSent", productMessages.size(),
                "messages", productMessages
        ));
    }
}
