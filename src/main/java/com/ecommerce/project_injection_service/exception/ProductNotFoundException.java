package com.ecommerce.project_injection_service.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String sku) {
        super("Product not found for SKU: " + sku);
    }
}

