package com.ecommerce.project_injection_service.validation;

import com.ecommerce.project_injection_service.dto.ProductMessageDto;
import com.ecommerce.project_injection_service.model.ProductStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductValidation Unit Tests")
class ProductValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private ProductMessageDto createValidProduct() {
        return new ProductMessageDto(
                "101980",
                "101980-RED",
                "Classic Budding Rose",
                "1800FLOWERS",
                "Flowers",
                59.99,
                10,
                ProductStatus.ACTIVE
        );
    }

    @Test
    @DisplayName("Should pass validation when all fields are valid")
    void shouldPassValidationWhenProductIsValid() {
        ProductMessageDto product = createValidProduct();
        Set<ConstraintViolation<ProductMessageDto>> violations = validator.validate(product);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should pass validation with zero price and zero inventory")
    void shouldPassValidationWithZeroPriceAndInventory() {
        ProductMessageDto product = new ProductMessageDto(
                "101980", "101980-RED", "Classic Budding Rose",
                "1800FLOWERS", "Flowers", 0.0, 0, ProductStatus.ACTIVE
        );
        Set<ConstraintViolation<ProductMessageDto>> violations = validator.validate(product);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when productId is blank")
    void shouldFailValidationWhenProductIdIsBlank() {
        ProductMessageDto product = new ProductMessageDto(
                "   ", "101980-RED", "Classic Budding Rose",
                "1800FLOWERS", "Flowers", 59.99, 10, ProductStatus.ACTIVE
        );
        Set<ConstraintViolation<ProductMessageDto>> violations = validator.validate(product);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("productId"));
    }

    @Test
    @DisplayName("Should fail validation when sku is blank")
    void shouldFailValidationWhenSkuIsBlank() {
        ProductMessageDto product = new ProductMessageDto(
                "101980", "", "Classic Budding Rose",
                "1800FLOWERS", "Flowers", 59.99, 10, ProductStatus.ACTIVE
        );
        Set<ConstraintViolation<ProductMessageDto>> violations = validator.validate(product);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("sku"));
    }

    @Test
    @DisplayName("Should fail validation when name is blank")
    void shouldFailValidationWhenNameIsBlank() {
        ProductMessageDto product = new ProductMessageDto(
                "101980", "101980-RED", "",
                "1800FLOWERS", "Flowers", 59.99, 10, ProductStatus.ACTIVE
        );
        Set<ConstraintViolation<ProductMessageDto>> violations = validator.validate(product);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    @DisplayName("Should fail validation when price is negative or null")
    void shouldFailValidationWhenPriceIsNegativeOrNull() {
        ProductMessageDto negPrice = new ProductMessageDto(
                "101980", "101980-RED", "Classic Budding Rose",
                "1800FLOWERS", "Flowers", -0.01, 10, ProductStatus.ACTIVE
        );
        ProductMessageDto nullPrice = new ProductMessageDto(
                "101980", "101980-RED", "Classic Budding Rose",
                "1800FLOWERS", "Flowers", null, 10, ProductStatus.ACTIVE
        );

        assertThat(validator.validate(negPrice)).isNotEmpty();
        assertThat(validator.validate(nullPrice)).isNotEmpty();
    }

    @Test
    @DisplayName("Should fail validation when inventory is negative or null")
    void shouldFailValidationWhenInventoryIsNegativeOrNull() {
        ProductMessageDto negInv = new ProductMessageDto(
                "101980", "101980-RED", "Classic Budding Rose",
                "1800FLOWERS", "Flowers", 59.99, -1, ProductStatus.ACTIVE
        );
        ProductMessageDto nullInv = new ProductMessageDto(
                "101980", "101980-RED", "Classic Budding Rose",
                "1800FLOWERS", "Flowers", 59.99, null, ProductStatus.ACTIVE
        );

        assertThat(validator.validate(negInv)).isNotEmpty();
        assertThat(validator.validate(nullInv)).isNotEmpty();
    }

    @Test
    @DisplayName("Should fail validation when status is null")
    void shouldFailValidationWhenStatusIsNull() {
        ProductMessageDto nullStatus = new ProductMessageDto(
                "101980", "101980-RED", "Classic Budding Rose",
                "1800FLOWERS", "Flowers", 59.99, 10, null
        );

        Set<ConstraintViolation<ProductMessageDto>> violations = validator.validate(nullStatus);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("status"));
    }
}
