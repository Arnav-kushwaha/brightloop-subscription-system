package com.brightloop.system.quotation;

import java.math.BigDecimal;

public class QuotationItemDTO {

    private Long id;
    private Long productId;
    private String productName;
    private Long variantId;
    private String variantName;
    private Long planId;
    private String planName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public QuotationItemDTO() {
    }

    public static QuotationItemDTO fromEntity(QuotationItem item) {
        QuotationItemDTO dto = new QuotationItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
        dto.setProductName(item.getProduct() != null ? item.getProduct().getName() : null);
        dto.setVariantId(item.getVariant() != null ? item.getVariant().getId() : null);
        dto.setVariantName(item.getVariant() != null ? item.getVariant().getVariantName() : null);
        dto.setPlanId(item.getPlan() != null ? item.getPlan().getId() : null);
        dto.setPlanName(item.getPlan() != null ? item.getPlan().getPlanName() : null);
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setLineTotal(item.getLineTotal());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}
