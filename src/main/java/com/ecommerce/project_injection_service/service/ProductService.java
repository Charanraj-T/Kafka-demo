package com.ecommerce.project_injection_service.service;

import com.ecommerce.project_injection_service.dto.ProductMessageDto;
import com.ecommerce.project_injection_service.dto.ProductResponseDto;
import com.ecommerce.project_injection_service.model.ProductDocument;

import java.util.List;

public interface ProductService {

    ProductDocument processProductUpdate(ProductMessageDto message);

    ProductResponseDto getProductBySku(String sku);

    List<ProductResponseDto> getAllProducts();
}
