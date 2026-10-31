package com.brightloop.finance.controller;

import com.brightloop.finance.entity.Payment;
import com.brightloop.finance.service.PaymentService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public Payment recordPayment(@RequestBody Payment payment) {
        return paymentService.recordPayment(payment);
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/invoice/{invoiceId}")
    public List<Payment> getPaymentsByInvoice(
            @PathVariable Long invoiceId) {
        return paymentService.getPaymentsByInvoice(invoiceId);
    }
}