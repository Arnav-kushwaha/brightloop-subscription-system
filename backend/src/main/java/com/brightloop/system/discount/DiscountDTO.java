package com.brightloop.system.discount;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class DiscountDTO {

    private Long id;

    @NotBlank(message = "Discount code is required")
    private String code;

    @NotNull(message = "Discount type is required")
    private DiscountType type;

    @NotNull(message = "Discount amount is required")
    @Positive(message = "Discount amount must be positive")
    private BigDecimal amount;

    private BigDecimal minPurchaseAmount = BigDecimal.ZERO;
    private int minQuantity = 1;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private int usageLimit = 100;
    private int timesUsed = 0;
    private boolean active = true;

    public DiscountDTO() {
    }

    public static DiscountDTO fromEntity(Discount discount) {
        DiscountDTO dto = new DiscountDTO();
        dto.setId(discount.getId());
        dto.setCode(discount.getCode());
        dto.setType(discount.getType());
        dto.setAmount(discount.getAmount());
        dto.setMinPurchaseAmount(discount.getMinPurchaseAmount());
        dto.setMinQuantity(discount.getMinQuantity());
        dto.setStartDate(discount.getStartDate());
        dto.setEndDate(discount.getEndDate());
        dto.setUsageLimit(discount.getUsageLimit());
        dto.setTimesUsed(discount.getTimesUsed());
        dto.setActive(discount.isActive());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public DiscountType getType() {
        return type;
    }

    public void setType(DiscountType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getMinPurchaseAmount() {
        return minPurchaseAmount;
    }

    public void setMinPurchaseAmount(BigDecimal minPurchaseAmount) {
        this.minPurchaseAmount = minPurchaseAmount;
    }

    public int getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(int minQuantity) {
        this.minQuantity = minQuantity;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(int usageLimit) {
        this.usageLimit = usageLimit;
    }

    public int getTimesUsed() {
        return timesUsed;
    }

    public void setTimesUsed(int timesUsed) {
        this.timesUsed = timesUsed;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
