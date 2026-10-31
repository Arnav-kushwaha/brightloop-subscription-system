package com.brightloop.system.subscription;

import com.brightloop.system.auth.User;
import com.brightloop.system.common.BaseEntity;
import com.brightloop.system.plan.BillingInterval;
import com.brightloop.system.plan.SubscriptionPlan;
import com.brightloop.system.product.Product;
import com.brightloop.system.product.ProductVariant;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "subscriptions")
public class Subscription extends BaseEntity {

    @Column(name = "subscription_number", nullable = false, unique = true, length = 50)
    private String subscriptionNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 100)
    private String customerEmail;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Column(nullable = false)
    private int quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "recurring_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal recurringAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_interval", nullable = false, length = 20)
    private BillingInterval billingInterval;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "next_billing_date", nullable = false)
    private LocalDate nextBillingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "source_quotation_id")
    private Long sourceQuotationId;

    public Subscription() {
    }

    public Subscription(String subscriptionNumber, User customer, SubscriptionPlan plan, Product product,
                        ProductVariant variant, int quantity, BigDecimal unitPrice, BigDecimal recurringAmount,
                        BillingInterval billingInterval, LocalDate startDate, LocalDate expiryDate,
                        LocalDate nextBillingDate, SubscriptionStatus status, Long sourceQuotationId) {
        this.subscriptionNumber = subscriptionNumber;
        this.customer = customer;
        this.customerName = customer != null ? customer.getFullName() : "";
        this.customerEmail = customer != null ? customer.getEmail() : "";
        this.plan = plan;
        this.product = product;
        this.variant = variant;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.recurringAmount = recurringAmount;
        this.billingInterval = billingInterval;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.nextBillingDate = nextBillingDate;
        this.status = status != null ? status : SubscriptionStatus.ACTIVE;
        this.sourceQuotationId = sourceQuotationId;
    }

    // Getters and Setters
    public String getSubscriptionNumber() {
        return subscriptionNumber;
    }

    public void setSubscriptionNumber(String subscriptionNumber) {
        this.subscriptionNumber = subscriptionNumber;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
        if (customer != null) {
            this.customerName = customer.getFullName();
            this.customerEmail = customer.getEmail();
        }
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

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public ProductVariant getVariant() {
        return variant;
    }

    public void setVariant(ProductVariant variant) {
        this.variant = variant;
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
