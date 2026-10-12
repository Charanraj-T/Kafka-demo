package com.ecommerce.project_injection_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class ProductDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sku;

    private String productId;
    private String name;
    private String brand;
    private String category;
    private Double price;
    private Integer inventory;
    private ProductStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    public ProductDocument(String sku, String productId, String name, String brand,
                           String category, Double price, Integer inventory, ProductStatus status) {
        this.sku = sku;
        this.productId = productId;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.price = price;
        this.inventory = inventory;
        this.status = status;
    }
}
