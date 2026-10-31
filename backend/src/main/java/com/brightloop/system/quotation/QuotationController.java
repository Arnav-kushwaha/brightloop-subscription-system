package com.brightloop.system.quotation;

import com.brightloop.system.common.ApiResponse;
import com.brightloop.system.subscription.SubscriptionDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotations")
public class QuotationController {

    private final QuotationService quotationService;

    public QuotationController(QuotationService quotationService) {
        this.quotationService = quotationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<QuotationDTO>>> getAllQuotations() {
        List<QuotationDTO> quotations = quotationService.getAllQuotations();
        return ResponseEntity.ok(ApiResponse.success(quotations));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuotationDTO>> getQuotationById(@PathVariable Long id) {
        QuotationDTO quotation = quotationService.getQuotationById(id);
        return ResponseEntity.ok(ApiResponse.success(quotation));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<QuotationDTO>> createQuotation(@Valid @RequestBody CreateQuotationRequest request) {
        QuotationDTO created = quotationService.createQuotation(request);
        return new ResponseEntity<>(ApiResponse.success(created, "Quotation created successfully"), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<QuotationDTO>> confirmQuotation(@PathVariable Long id) {
        QuotationDTO confirmed = quotationService.confirmQuotation(id);
        return ResponseEntity.ok(ApiResponse.success(confirmed, "Quotation confirmed successfully"));
    }

    @PostMapping("/{id}/convert-to-subscription")
    public ResponseEntity<ApiResponse<List<SubscriptionDTO>>> convertToSubscription(@PathVariable Long id) {
        List<SubscriptionDTO> subscriptions = quotationService.convertToSubscription(id);
        return ResponseEntity.ok(ApiResponse.success(subscriptions, "Quotation successfully converted to active subscriptions"));
    }
}
