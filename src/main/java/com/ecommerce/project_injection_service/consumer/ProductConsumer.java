package com.ecommerce.project_injection_service.consumer;

import com.ecommerce.project_injection_service.dto.ProductMessageDto;
import com.ecommerce.project_injection_service.model.ProductDocument;
import com.ecommerce.project_injection_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductConsumer {

    private final ProductService productService;

    public ProductConsumer(ProductService productService) {
        this.productService = productService;
    }

    @KafkaListener(topics = "${app.kafka.topics.in:products.in}")
    public void consumeProduct(
            @Valid @Payload ProductMessageDto message,
            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(name = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
            @Header(name = KafkaHeaders.OFFSET, required = false) Long offset
    ) {
        log.info("[RECEIVED] Inbound product update: SKU='{}', partition={}, offset={}",
                message.sku(), partition, offset);

        ProductDocument saved = productService.processProductUpdate(message);
        log.info("[SUCCESS] Processed product: SKU='{}', mongoId='{}'", saved.getSku(), saved.getId());
    }
}

