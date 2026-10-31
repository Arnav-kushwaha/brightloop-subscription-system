package com.brightloop.finance.service;

import com.brightloop.finance.entity.Invoice;
import com.brightloop.finance.entity.Quotation;
import com.brightloop.finance.repository.InvoiceRepository;
import com.brightloop.finance.repository.QuotationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final QuotationRepository quotationRepository;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            QuotationRepository quotationRepository) {
        this.invoiceRepository = invoiceRepository;
        this.quotationRepository = quotationRepository;
    }

    @Transactional
    public Invoice generateInvoice(Long quotationId) {

        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Quotation not found"));

        BigDecimal amount = quotation.getAmount();
        BigDecimal taxRate = quotation.getTaxRate();
        BigDecimal discount = quotation.getDiscount();

        BigDecimal taxAmount = amount.multiply(taxRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        Invoice invoice = new Invoice();

        invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
        invoice.setQuotationId(quotation.getId());
        invoice.setCustomerName(quotation.getCustomerName());
        invoice.setAmount(amount);
        invoice.setTaxAmount(taxAmount);
        invoice.setDiscount(discount);
        invoice.setTotalAmount(quotation.getTotalAmount());
        invoice.setStatus("PENDING");
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));

        return invoiceRepository.save(invoice);
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invoice not found"));
    }
}