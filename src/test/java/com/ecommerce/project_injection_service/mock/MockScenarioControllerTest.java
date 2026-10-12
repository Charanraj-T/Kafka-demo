package com.ecommerce.project_injection_service.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("MockScenarioController Unit Tests")
class MockScenarioControllerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockScenarioController controller = new MockScenarioController(kafkaTemplate);
        ReflectionTestUtils.setField(controller, "inputTopic", "products.in");
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("POST /mock/publish should publish custom message to Kafka")
    void shouldPublishCustomMessage() throws Exception {
        String json = """
                {
                  "sku": "CUSTOM-1",
                  "productId": "P-1"
                }
                """;

        mockMvc.perform(post("/mock/publish")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.key").value("CUSTOM-1"));

        verify(kafkaTemplate).send(eq("products.in"), eq("CUSTOM-1"), any(Map.class));
    }

    @Test
    @DisplayName("POST /mock/scenario should load mock-products.json and publish all messages")
    void shouldRunScenarioFromMockFile() throws Exception {
        mockMvc.perform(post("/mock/scenario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.totalMessagesSent").value(3));

        verify(kafkaTemplate, atLeastOnce()).send(eq("products.in"), any(), any());
    }
}

