package com.ecommerce.project_injection_service.dto;

import com.ecommerce.project_injection_service.model.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductMessageDto(
        @NotBlank(message = "productId is required")
        String productId,

        @NotBlank(message = "sku is required")
        String sku,

        @NotBlank(message = "name is required")
        String name,

        String brand,
        String category,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "price must be greater than or equal to 0")
        Double price,

        @NotNull(message = "inventory is required")
        @Min(value = 0, message = "inventory must be greater than or equal to 0")
        Integer inventory,

        @NotNull(message = "status is required")
        ProductStatus status
) {}

