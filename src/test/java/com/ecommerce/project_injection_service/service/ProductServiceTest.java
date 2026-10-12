package com.ecommerce.project_injection_service.service;

import com.ecommerce.project_injection_service.dto.ProductMessageDto;
import com.ecommerce.project_injection_service.dto.ProductResponseDto;
import com.ecommerce.project_injection_service.exception.ProductNotFoundException;
import com.ecommerce.project_injection_service.model.ProductDocument;
import com.ecommerce.project_injection_service.model.ProductStatus;
import com.ecommerce.project_injection_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductMessageDto sampleMessage;

    @BeforeEach
    void setUp() {
        sampleMessage = new ProductMessageDto(
                "101980", "101980-RED", "Classic Budding Rose",
                "1800FLOWERS", "Flowers", 59.99, 10, ProductStatus.ACTIVE
        );
    }

    @Test
    @DisplayName("Should create a new product document in MongoDB when SKU does not exist")
    void createNewProductWhenSkuDoesNotExist() {
        when(productRepository.findBySku("101980-RED")).thenReturn(Optional.empty());
        when(productRepository.save(any(ProductDocument.class))).thenAnswer(invocation -> {
            ProductDocument doc = invocation.getArgument(0);
            doc.setId("mongo-id-123");
            return doc;
        });

        ProductDocument result = productService.processProductUpdate(sampleMessage);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("mongo-id-123");
        assertThat(result.getSku()).isEqualTo("101980-RED");
        assertThat(result.getProductId()).isEqualTo("101980");
        assertThat(result.getName()).isEqualTo("Classic Budding Rose");
        assertThat(result.getPrice()).isEqualTo(59.99);
        assertThat(result.getInventory()).isEqualTo(10);
        assertThat(result.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        verify(productRepository, times(1)).findBySku("101980-RED");
        verify(productRepository, times(1)).save(any(ProductDocument.class));
    }

    @Test
    @DisplayName("Should update existing product document in MongoDB when SKU already exists")
    void updateExistingProductWhenSkuExists() {
        ProductDocument existingDoc = new ProductDocument(
                "101980-RED", "101980", "Old Name", "1800FLOWERS", "Flowers", 49.99, 5, ProductStatus.INACTIVE
        );
        existingDoc.setId("mongo-existing-id");

        when(productRepository.findBySku("101980-RED")).thenReturn(Optional.of(existingDoc));
        when(productRepository.save(any(ProductDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductDocument result = productService.processProductUpdate(sampleMessage);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("mongo-existing-id");
        assertThat(result.getName()).isEqualTo("Classic Budding Rose");
        assertThat(result.getPrice()).isEqualTo(59.99);
        assertThat(result.getInventory()).isEqualTo(10);
        assertThat(result.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(result.getUpdatedAt()).isNotNull();

        verify(productRepository, times(1)).findBySku("101980-RED");
        verify(productRepository, times(1)).save(existingDoc);
    }

    @Test
    @DisplayName("Should retrieve product by SKU when product exists")
    void getProductBySkuSuccess() {
        ProductDocument doc = new ProductDocument(
                "101980-RED", "101980", "Classic Budding Rose", "1800FLOWERS", "Flowers", 59.99, 10, ProductStatus.ACTIVE
        );
        doc.setId("mongo-id-123");

        when(productRepository.findBySku("101980-RED")).thenReturn(Optional.of(doc));

        ProductResponseDto response = productService.getProductBySku("101980-RED");

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("mongo-id-123");
        assertThat(response.sku()).isEqualTo("101980-RED");
        assertThat(response.name()).isEqualTo("Classic Budding Rose");

        verify(productRepository, times(1)).findBySku("101980-RED");
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when SKU does not exist")
    void getProductBySkuNotFound() {
        when(productRepository.findBySku("NOT-FOUND")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductBySku("NOT-FOUND"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found for SKU: NOT-FOUND");

        verify(productRepository, times(1)).findBySku("NOT-FOUND");
    }

    @Test
    @DisplayName("Should return all products")
    void getAllProducts() {
        ProductDocument doc1 = new ProductDocument("SKU-1", "P-1", "Rose", "B1", "C1", 10.0, 5, ProductStatus.ACTIVE);
        ProductDocument doc2 = new ProductDocument("SKU-2", "P-2", "Lily", "B1", "C1", 20.0, 8, ProductStatus.INACTIVE);

        when(productRepository.findAll()).thenReturn(List.of(doc1, doc2));

        List<ProductResponseDto> result = productService.getAllProducts();

        assertThat(result).hasSize(2);
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should propagate transient database failure to trigger retry")
    void shouldPropagateTransientDatabaseFailure() {
        when(productRepository.findBySku("101980-RED"))
                .thenThrow(new DataAccessResourceFailureException("Database connection timeout"));

        assertThatThrownBy(() -> productService.processProductUpdate(sampleMessage))
                .isInstanceOf(DataAccessResourceFailureException.class);
    }
}
