package com.ecommerce.project_injection_service.controller;

import com.ecommerce.project_injection_service.dto.ProductResponseDto;
import com.ecommerce.project_injection_service.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{sku}")
    public ResponseEntity<ProductResponseDto> getProductBySku(@PathVariable String sku) {
        log.info("REST API GET /products/{}", sku);
        return ResponseEntity.ok(productService.getProductBySku(sku));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
}
