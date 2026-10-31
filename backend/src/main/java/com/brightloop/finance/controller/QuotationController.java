package com.brightloop.finance.controller;

import com.brightloop.finance.entity.Quotation;
import com.brightloop.finance.service.QuotationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotations")
@CrossOrigin(origins = "*")
public class QuotationController {

    private final QuotationService quotationService;

    public QuotationController(QuotationService quotationService) {
        this.quotationService = quotationService;
    }

    // Create quotation
    @PostMapping
    public ResponseEntity<Quotation> createQuotation(
            @RequestBody Quotation quotation) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quotationService.createQuotation(quotation));
    }

    // Get all quotations
    @GetMapping
    public ResponseEntity<List<Quotation>> getAllQuotations() {
        return ResponseEntity.ok(
                quotationService.getAllQuotations());
    }

    // Get quotation by ID
    @GetMapping("/{id}")
    public ResponseEntity<Quotation> getQuotationById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                quotationService.getQuotationById(id));
    }

    // Delete quotation
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuotation(
            @PathVariable Long id) {

        quotationService.deleteQuotation(id);
        return ResponseEntity.noContent().build();
    }
}