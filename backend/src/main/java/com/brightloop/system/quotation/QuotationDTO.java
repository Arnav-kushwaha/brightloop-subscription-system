package com.brightloop.system.quotation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuotationDTO {

    private Long id;
    private String quotationNumber;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private LocalDate validUntil;
    private QuotationStatus status;
    private BigDecimal subtotal;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String notes;
    private List<QuotationItemDTO> items = new ArrayList<>();

    public QuotationDTO() {
    }

    public static QuotationDTO fromEntity(Quotation quotation) {
        QuotationDTO dto = new QuotationDTO();
        dto.setId(quotation.getId());
        dto.setQuotationNumber(quotation.getQuotationNumber());
        dto.setCustomerId(quotation.getCustomer() != null ? quotation.getCustomer().getId() : null);
        dto.setCustomerName(quotation.getCustomerName());
        dto.setCustomerEmail(quotation.getCustomerEmail());
        dto.setValidUntil(quotation.getValidUntil());
        dto.setStatus(quotation.getStatus());
        dto.setSubtotal(quotation.getSubtotal());
        dto.setTaxRate(quotation.getTaxRate());
        dto.setTaxAmount(quotation.getTaxAmount());
        dto.setDiscountAmount(quotation.getDiscountAmount());
        dto.setTotalAmount(quotation.getTotalAmount());
        dto.setNotes(quotation.getNotes());

        if (quotation.getItems() != null) {
            dto.setItems(quotation.getItems().stream()
                    .map(QuotationItemDTO::fromEntity)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuotationNumber() {
        return quotationNumber;
    }

    public void setQuotationNumber(String quotationNumber) {
        this.quotationNumber = quotationNumber;
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

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public QuotationStatus getStatus() {
        return status;
    }

    public void setStatus(QuotationStatus status) {
        this.status = status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<QuotationItemDTO> getItems() {
        return items;
    }

    public void setItems(List<QuotationItemDTO> items) {
        this.items = items;
    }
}
