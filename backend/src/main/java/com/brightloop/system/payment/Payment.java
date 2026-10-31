package com.brightloop.system.payment;

import com.brightloop.system.common.BaseEntity;
import com.brightloop.system.invoice.Invoice;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Column(name = "payment_reference", nullable = false, unique = true, length = 50)
    private String paymentReference;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "recorded_by", length = 100)
    private String recordedBy;

    public Payment() {
    }

    public Payment(String paymentReference, Invoice invoice, BigDecimal amount,
                   PaymentMethod paymentMethod, LocalDateTime paymentDate, String notes, String recordedBy) {
        this.paymentReference = paymentReference;
        this.invoice = invoice;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate != null ? paymentDate : LocalDateTime.now();
        this.notes = notes;
        this.recordedBy = recordedBy;
    }

    // Getters and Setters
    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }
}
