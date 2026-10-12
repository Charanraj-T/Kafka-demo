package com.ecommerce.project_injection_service.dto;

import com.ecommerce.project_injection_service.model.ProductDocument;
import com.ecommerce.project_injection_service.model.ProductStatus;

import java.time.Instant;

public record ProductResponseDto(
        String id,
        String sku,
        String productId,
        String name,
        String brand,
        String category,
        Double price,
        Integer inventory,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductResponseDto fromDocument(ProductDocument doc) {
        return new ProductResponseDto(
                doc.getId(),
                doc.getSku(),
                doc.getProductId(),
                doc.getName(),
                doc.getBrand(),
                doc.getCategory(),
                doc.getPrice(),
                doc.getInventory(),
                doc.getStatus(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }
}

