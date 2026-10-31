package com.brightloop.system.payment;

import com.brightloop.system.invoice.Invoice;
import com.brightloop.system.invoice.InvoiceRepository;
import com.brightloop.system.invoice.InvoiceStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAllOrderByIdDesc().stream()
                .map(PaymentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public PaymentDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with id: " + id));
        return PaymentDTO.fromEntity(payment);
    }

    public List<PaymentDTO> getPaymentsByInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId).stream()
                .map(PaymentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentDTO recordPayment(RecordPaymentRequest request) {
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with id: " + request.getInvoiceId()));

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Cannot record payment for a cancelled invoice");
        }

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Invoice is already fully paid");
        }

        BigDecimal payAmount = request.getAmount();
        if (payAmount.compareTo(invoice.getOutstandingAmount()) > 0) {
            throw new IllegalArgumentException("Payment amount ($" + payAmount +
                    ") exceeds remaining outstanding balance of $" + invoice.getOutstandingAmount());
        }

        String recordedBy = "System";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            recordedBy = auth.getName();
        }

        String paymentRef = generatePaymentReference();
        Payment payment = new Payment(
                paymentRef,
                invoice,
                payAmount,
                request.getPaymentMethod(),
                LocalDateTime.now(),
                request.getNotes(),
                recordedBy
        );

        Payment savedPayment = paymentRepository.save(payment);

        // Update Invoice amounts
        BigDecimal newPaidAmount = invoice.getPaidAmount().add(payAmount);
        BigDecimal newOutstanding = invoice.getTotalAmount().subtract(newPaidAmount);
        invoice.setPaidAmount(newPaidAmount);
        invoice.setOutstandingAmount(newOutstanding.max(BigDecimal.ZERO));

        if (invoice.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.CONFIRMED); // partially paid
        }

        invoiceRepository.save(invoice);
        return PaymentDTO.fromEntity(savedPayment);
    }

    private String generatePaymentReference() {
        return "PAY-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
