package com.brightloop.system.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProductVariantDTO {

    private Long id;

    @NotBlank(message = "Variant name is required")
    private String variantName;

    @NotNull(message = "Additional price is required")
    private BigDecimal additionalPrice = BigDecimal.ZERO;

    private String sku;

    public ProductVariantDTO() {
    }

    public ProductVariantDTO(Long id, String variantName, BigDecimal additionalPrice, String sku) {
        this.id = id;
        this.variantName = variantName;
        this.additionalPrice = additionalPrice;
        this.sku = sku;
    }

    public static ProductVariantDTO fromEntity(ProductVariant variant) {
        return new ProductVariantDTO(
                variant.getId(),
                variant.getVariantName(),
                variant.getAdditionalPrice(),
                variant.getSku()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public BigDecimal getAdditionalPrice() {
        return additionalPrice;
    }

    public void setAdditionalPrice(BigDecimal additionalPrice) {
        this.additionalPrice = additionalPrice;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }
}
