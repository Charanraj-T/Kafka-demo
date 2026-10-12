package com.ecommerce.project_injection_service.consumer;

import com.ecommerce.project_injection_service.dto.ProductMessageDto;
import com.ecommerce.project_injection_service.model.ProductDocument;
import com.ecommerce.project_injection_service.model.ProductStatus;
import com.ecommerce.project_injection_service.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductConsumer Unit Tests")
class ProductConsumerTest {

    @Mock
    private ProductService productService;

    private ProductConsumer productConsumer;

    @BeforeEach
    void setUp() {
        productConsumer = new ProductConsumer(productService);
    }

    @Test
    @DisplayName("Should successfully consume and persist a valid product message")
    void consumeValidProduct() {
        ProductMessageDto validDto = new ProductMessageDto(
                "101980", "101980-RED", "Classic Budding Rose",
                "1800FLOWERS", "Flowers", 59.99, 10, ProductStatus.ACTIVE
        );

        ProductDocument savedDoc = new ProductDocument(
                "101980-RED", "101980", "Classic Budding Rose",
                "1800FLOWERS", "Flowers", 59.99, 10, ProductStatus.ACTIVE
        );
        savedDoc.setId("mongo-123");
        when(productService.processProductUpdate(any(ProductMessageDto.class))).thenReturn(savedDoc);

        productConsumer.consumeProduct(validDto, "101980-RED", 0, 100L);

        verify(productService, times(1)).processProductUpdate(validDto);
    }

    @Test
    @DisplayName("Should propagate transient processing/database exceptions for retry handling")
    void consumeTransientDatabaseFailurePropagatesException() {
        ProductMessageDto validDto = new ProductMessageDto(
                "101980", "101980-RED", "Classic Budding Rose",
                "1800FLOWERS", "Flowers", 59.99, 10, ProductStatus.ACTIVE
        );

        when(productService.processProductUpdate(any(ProductMessageDto.class)))
                .thenThrow(new DataAccessResourceFailureException("Transient MongoDB network blip"));

        assertThatThrownBy(() -> productConsumer.consumeProduct(validDto, "101980-RED", 0, 100L))
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessage("Transient MongoDB network blip");

        verify(productService, times(1)).processProductUpdate(validDto);
    }
}

