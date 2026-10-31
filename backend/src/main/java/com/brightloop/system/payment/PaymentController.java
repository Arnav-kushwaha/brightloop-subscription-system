package com.brightloop.system.payment;

import com.brightloop.system.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getAllPayments() {
        List<PaymentDTO> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentById(@PathVariable Long id) {
        PaymentDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/by-invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getPaymentsByInvoice(@PathVariable Long invoiceId) {
        List<PaymentDTO> payments = paymentService.getPaymentsByInvoice(invoiceId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDTO>> recordPayment(@Valid @RequestBody RecordPaymentRequest request) {
        PaymentDTO payment = paymentService.recordPayment(request);
        return new ResponseEntity<>(ApiResponse.success(payment, "Payment recorded successfully"), HttpStatus.CREATED);
    }
}
