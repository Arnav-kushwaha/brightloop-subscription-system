package com.brightloop.finance.service;

import com.brightloop.finance.entity.Quotation;
import com.brightloop.finance.repository.QuotationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class QuotationService {

    private final QuotationRepository quotationRepository;

    public QuotationService(QuotationRepository quotationRepository) {
        this.quotationRepository = quotationRepository;
    }

    @Transactional
    public Quotation createQuotation(Quotation quotation) {

        if (quotation.getCustomerName() == null ||
                quotation.getCustomerName().isBlank()) {
            throw new IllegalArgumentException("Customer name is required");
        }

        BigDecimal amount = quotation.getAmount();
        BigDecimal taxRate = quotation.getTaxRate();
        BigDecimal discount = quotation.getDiscount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        if (taxRate == null ||
                taxRate.compareTo(BigDecimal.ZERO) < 0 ||
                taxRate.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 100");
        }

        if (discount == null) {
            discount = BigDecimal.ZERO;
        }

        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount cannot be negative");
        }

        BigDecimal tax = amount.multiply(taxRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal total = amount.add(tax).subtract(discount);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount is too high");
        }

        quotation.setDiscount(discount);
        quotation.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        quotation.setCreatedDate(LocalDate.now());
        quotation.setStatus("DRAFT");

        if (quotation.getQuotationNumber() == null ||
                quotation.getQuotationNumber().isBlank()) {
            quotation.setQuotationNumber("QUO-" +
                    System.currentTimeMillis());
        }

        return quotationRepository.save(quotation);
    }

    public List<Quotation> getAllQuotations() {
        return quotationRepository.findAll();
    }

    public Quotation getQuotationById(Long id) {
        return quotationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Quotation not found"));
    }

    public void deleteQuotation(Long id) {
        if (!quotationRepository.existsById(id)) {
            throw new RuntimeException("Quotation not found");
        }

        quotationRepository.deleteById(id);
    }
}