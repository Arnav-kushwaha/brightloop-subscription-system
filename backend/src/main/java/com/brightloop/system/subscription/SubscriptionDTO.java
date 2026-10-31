package com.brightloop.system.subscription;

import com.brightloop.system.plan.BillingInterval;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class SubscriptionDTO {

    private Long id;
    private String subscriptionNumber;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Long planId;
    private String planName;
    private Long productId;
    private String productName;
    private Long variantId;
    private String variantName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal recurringAmount;
    private BillingInterval billingInterval;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private LocalDate nextBillingDate;
    private SubscriptionStatus status;
    private Long sourceQuotationId;

    public SubscriptionDTO() {
    }

    public static SubscriptionDTO fromEntity(Subscription sub) {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setId(sub.getId());
        dto.setSubscriptionNumber(sub.getSubscriptionNumber());
        dto.setCustomerId(sub.getCustomer() != null ? sub.getCustomer().getId() : null);
        dto.setCustomerName(sub.getCustomerName());
        dto.setCustomerEmail(sub.getCustomerEmail());
        dto.setPlanId(sub.getPlan() != null ? sub.getPlan().getId() : null);
        dto.setPlanName(sub.getPlan() != null ? sub.getPlan().getPlanName() : null);
        dto.setProductId(sub.getProduct() != null ? sub.getProduct().getId() : null);
        dto.setProductName(sub.getProduct() != null ? sub.getProduct().getName() : null);
        dto.setVariantId(sub.getVariant() != null ? sub.getVariant().getId() : null);
        dto.setVariantName(sub.getVariant() != null ? sub.getVariant().getVariantName() : null);
        dto.setQuantity(sub.getQuantity());
        dto.setUnitPrice(sub.getUnitPrice());
        dto.setRecurringAmount(sub.getRecurringAmount());
        dto.setBillingInterval(sub.getBillingInterval());
        dto.setStartDate(sub.getStartDate());
        dto.setExpiryDate(sub.getExpiryDate());
        dto.setNextBillingDate(sub.getNextBillingDate());
        dto.setStatus(sub.getStatus());
        dto.setSourceQuotationId(sub.getSourceQuotationId());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubscriptionNumber() {
        return subscriptionNumber;
    }

    public void setSubscriptionNumber(String subscriptionNumber) {
        this.subscriptionNumber = subscriptionNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
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

    public BigDecimal getRecurringAmount() {
        return recurringAmount;
    }

    public void setRecurringAmount(BigDecimal recurringAmount) {
        this.recurringAmount = recurringAmount;
    }

    public BillingInterval getBillingInterval() {
        return billingInterval;
    }

    public void setBillingInterval(BillingInterval billingInterval) {
        this.billingInterval = billingInterval;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDate getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(LocalDate nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public Long getSourceQuotationId() {
        return sourceQuotationId;
    }

    public void setSourceQuotationId(Long sourceQuotationId) {
        this.sourceQuotationId = sourceQuotationId;
    }
}
