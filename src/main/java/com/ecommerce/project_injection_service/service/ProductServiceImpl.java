package com.ecommerce.project_injection_service.service;

import com.ecommerce.project_injection_service.dto.ProductMessageDto;
import com.ecommerce.project_injection_service.dto.ProductResponseDto;
import com.ecommerce.project_injection_service.exception.ProductNotFoundException;
import com.ecommerce.project_injection_service.model.ProductDocument;
import com.ecommerce.project_injection_service.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductDocument processProductUpdate(ProductMessageDto message) {
        String sku = message.sku();

        ProductDocument document = productRepository.findBySku(sku)
                .orElseGet(() -> {
                    log.info("[CREATE PRODUCT] Creating new product document for SKU: {}, productId: {}", sku, message.productId());
                    ProductDocument newDoc = new ProductDocument();
                    newDoc.setSku(sku);
                    newDoc.setCreatedAt(Instant.now());
                    return newDoc;
                });

        if (document.getId() != null) {
            log.info("[UPDATE PRODUCT] Found existing product for SKU: {}. Updating document id: {}", sku, document.getId());
        }

        document.setProductId(message.productId());
        document.setName(message.name());
        document.setBrand(message.brand());
        document.setCategory(message.category());
        document.setPrice(message.price());
        document.setInventory(message.inventory());
        document.setStatus(message.status());
        document.setUpdatedAt(Instant.now());

        ProductDocument saved = productRepository.save(document);
        log.info("[SAVED] Product saved successfully. SKU: {}, productId: {}", saved.getSku(), saved.getProductId());
        return saved;
    }

    @Override
    public ProductResponseDto getProductBySku(String sku) {
        log.debug("Fetching product by SKU: {}", sku);
        return productRepository.findBySku(sku)
                .map(ProductResponseDto::fromDocument)
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }

    @Override
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponseDto::fromDocument)
                .toList();
    }
}
