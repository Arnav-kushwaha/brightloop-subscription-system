package com.brightloop.finance.service;

import com.brightloop.finance.entity.Invoice;
import com.brightloop.finance.entity.Payment;
import com.brightloop.finance.repository.InvoiceRepository;
import com.brightloop.finance.repository.PaymentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public Payment recordPayment(Payment payment) {

        Invoice invoice = invoiceRepository.findById(payment.getInvoiceId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invoice not found"));

        BigDecimal paymentAmount = payment.getAmount();

        if (paymentAmount == null ||
                paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid payment amount");
        }

        if (paymentAmount.compareTo(invoice.getTotalAmount()) > 0) {
            throw new IllegalArgumentException(
                    "Payment cannot exceed invoice total");
        }

        payment.setPaymentDate(LocalDate.now());

        Payment savedPayment = paymentRepository.save(payment);

        BigDecimal totalPaid = paymentRepository
                .findByInvoiceId(invoice.getId())
                .stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus("PAID");
        } else {
            invoice.setStatus("PENDING");
        }

        invoiceRepository.save(invoice);

        return savedPayment;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsByInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId);
    }
}