package com.ecommerce.project_injection_service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProjectInjectionServiceApplicationTests {

    @Test
    @DisplayName("Application main class should be present and loadable")
    void applicationClassLoads() {
        assertNotNull(ProjectInjectionServiceApplication.class);
    }

}
