package com.ecommerce.project_injection_service.repository;

import com.ecommerce.project_injection_service.model.ProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<ProductDocument, String> {

    Optional<ProductDocument> findBySku(String sku);

    boolean existsBySku(String sku);
}

