package com.ecommerce.project_injection_service.controller;

import com.ecommerce.project_injection_service.dto.ProductResponseDto;
import com.ecommerce.project_injection_service.exception.GlobalExceptionHandler;
import com.ecommerce.project_injection_service.exception.ProductNotFoundException;
import com.ecommerce.project_injection_service.model.ProductStatus;
import com.ecommerce.project_injection_service.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Unit Tests")
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /products/{sku} should return 200 OK when product exists")
    void shouldReturn200WhenProductExists() throws Exception {
        ProductResponseDto response = new ProductResponseDto(
                "mongo-123",
                "101980-RED",
                "101980",
                "Classic Budding Rose",
                "1800FLOWERS",
                "Flowers",
                59.99,
                10,
                ProductStatus.ACTIVE,
                Instant.parse("2026-10-11T12:00:00Z"),
                Instant.parse("2026-10-11T12:00:00Z")
        );

        when(productService.getProductBySku("101980-RED")).thenReturn(response);

        mockMvc.perform(get("/products/101980-RED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("101980-RED"))
                .andExpect(jsonPath("$.productId").value("101980"))
                .andExpect(jsonPath("$.name").value("Classic Budding Rose"))
                .andExpect(jsonPath("$.brand").value("1800FLOWERS"))
                .andExpect(jsonPath("$.category").value("Flowers"))
                .andExpect(jsonPath("$.price").value(59.99))
                .andExpect(jsonPath("$.inventory").value(10))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(productService).getProductBySku("101980-RED");
    }

    @Test
    @DisplayName("GET /products/{sku} should return 404 NOT FOUND when product does not exist")
    void shouldReturn404WhenProductNotFound() throws Exception {
        when(productService.getProductBySku("NON-EXISTENT"))
                .thenThrow(new ProductNotFoundException("NON-EXISTENT"));

        mockMvc.perform(get("/products/NON-EXISTENT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Product Not Found"))
                .andExpect(jsonPath("$.detail").value("Product not found for SKU: NON-EXISTENT"));

        verify(productService).getProductBySku("NON-EXISTENT");
    }

    @Test
    @DisplayName("GET /products should return all products")
    void shouldReturnAllProducts() throws Exception {
        ProductResponseDto p1 = new ProductResponseDto(
                "id-1", "SKU-1", "P-1", "Rose", "B1", "C1", 10.0, 5, ProductStatus.ACTIVE, Instant.now(), Instant.now()
        );
        when(productService.getAllProducts()).thenReturn(List.of(p1));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].sku").value("SKU-1"));

        verify(productService).getAllProducts();
    }

    @Test
    @DisplayName("GET /products/{sku} should return 500 when unexpected exception occurs")
    void shouldReturn500OnUnexpectedException() throws Exception {
        when(productService.getProductBySku("ERROR-SKU"))
                .thenThrow(new RuntimeException("Database down"));

        mockMvc.perform(get("/products/ERROR-SKU")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.title").value("Internal Server Error"));
    }
}
