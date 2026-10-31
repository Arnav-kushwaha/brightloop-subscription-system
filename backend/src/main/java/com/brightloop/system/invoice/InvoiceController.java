package com.brightloop.system.invoice;

import com.brightloop.system.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> getAllInvoices(
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) Long customerId
    ) {
        List<InvoiceDTO> invoices;
        if (status != null) {
            invoices = invoiceService.getInvoicesByStatus(status);
        } else if (customerId != null) {
            invoices = invoiceService.getInvoicesByCustomer(customerId);
        } else {
            invoices = invoiceService.getAllInvoices();
        }
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceDTO>> getInvoiceById(@PathVariable Long id) {
        InvoiceDTO invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceDTO>> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        InvoiceDTO created = invoiceService.createInvoice(request);
        return new ResponseEntity<>(ApiResponse.success(created, "Invoice created successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/generate-recurring")
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> generateRecurringInvoices() {
        List<InvoiceDTO> generated = invoiceService.generateRecurringInvoices();
        return ResponseEntity.ok(ApiResponse.success(generated, "Generated " + generated.size() + " recurring invoices"));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<InvoiceDTO>> confirmInvoice(@PathVariable Long id) {
        InvoiceDTO confirmed = invoiceService.confirmInvoice(id);
        return ResponseEntity.ok(ApiResponse.success(confirmed, "Invoice confirmed successfully"));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<InvoiceDTO>> cancelInvoice(@PathVariable Long id) {
        InvoiceDTO cancelled = invoiceService.cancelInvoice(id);
        return ResponseEntity.ok(ApiResponse.success(cancelled, "Invoice cancelled successfully"));
    }
}
