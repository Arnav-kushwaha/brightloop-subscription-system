package com.brightloop.system.plan;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PlanDTO {

    private Long id;

    @NotBlank(message = "Plan name is required")
    private String planName;

    @NotNull(message = "Billing interval is required")
    private BillingInterval billingInterval;

    @NotNull(message = "Interval count is required")
    @Min(value = 1, message = "Interval count must be at least 1")
    private int intervalCount = 1;

    @NotNull(message = "Base price is required")
    @PositiveOrZero(message = "Base price must be zero or positive")
    private BigDecimal basePrice;

    @NotNull(message = "Minimum quantity is required")
    @Min(value = 1, message = "Minimum quantity must be at least 1")
    private int minQuantity = 1;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;
    private String description;
    private boolean active = true;

    public PlanDTO() {
    }

    public PlanDTO(Long id, String planName, BillingInterval billingInterval, int intervalCount,
                   BigDecimal basePrice, int minQuantity, LocalDate startDate, LocalDate endDate,
                   String description, boolean active) {
        this.id = id;
        this.planName = planName;
        this.billingInterval = billingInterval;
        this.intervalCount = intervalCount;
        this.basePrice = basePrice;
        this.minQuantity = minQuantity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.active = active;
    }

    public static PlanDTO fromEntity(SubscriptionPlan plan) {
        return new PlanDTO(
                plan.getId(),
                plan.getPlanName(),
                plan.getBillingInterval(),
                plan.getIntervalCount(),
                plan.getBasePrice(),
                plan.getMinQuantity(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getDescription(),
                plan.isActive()
        );
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public BillingInterval getBillingInterval() {
        return billingInterval;
    }

    public void setBillingInterval(BillingInterval billingInterval) {
        this.billingInterval = billingInterval;
    }

    public int getIntervalCount() {
        return intervalCount;
    }

    public void setIntervalCount(int intervalCount) {
        this.intervalCount = intervalCount;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
