package com.brightloop.system.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductDTO {

    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotNull(message = "Product type is required")
    private ProductType type;

    @NotNull(message = "Selling price is required")
    @PositiveOrZero(message = "Selling price must be zero or positive")
    private BigDecimal sellingPrice;

    @NotNull(message = "Cost price is required")
    @PositiveOrZero(message = "Cost price must be zero or positive")
    private BigDecimal costPrice;

    private String notes;
    private boolean active;
    private List<ProductVariantDTO> variants = new ArrayList<>();

    public ProductDTO() {
    }

    public ProductDTO(Long id, String name, ProductType type, BigDecimal sellingPrice, BigDecimal costPrice,
                      String notes, boolean active, List<ProductVariantDTO> variants) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.sellingPrice = sellingPrice;
        this.costPrice = costPrice;
        this.notes = notes;
        this.active = active;
        this.variants = variants != null ? variants : new ArrayList<>();
    }

    public static ProductDTO fromEntity(Product product) {
        List<ProductVariantDTO> variantDTOs = product.getVariants() != null
                ? product.getVariants().stream().map(ProductVariantDTO::fromEntity).collect(Collectors.toList())
                : new ArrayList<>();

        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getType(),
                product.getSellingPrice(),
                product.getCostPrice(),
                product.getNotes(),
                product.isActive(),
                variantDTOs
        );
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductType getType() {
        return type;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<ProductVariantDTO> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariantDTO> variants) {
        this.variants = variants;
    }
}
