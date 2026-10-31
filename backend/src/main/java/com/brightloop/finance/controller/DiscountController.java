package com.brightloop.finance.controller;

import com.brightloop.finance.entity.Discount;
import com.brightloop.finance.service.DiscountService;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/discounts")
@CrossOrigin(origins = "*")
public class DiscountController {

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @PostMapping
    public Discount createDiscount(@RequestBody Discount discount) {
        return discountService.createDiscount(discount);
    }

    @GetMapping
    public List<Discount> getAllDiscounts() {
        return discountService.getAllDiscounts();
    }

    @GetMapping("/{id}/calculate")
    public BigDecimal calculateDiscount(
            @PathVariable Long id,
            @RequestParam BigDecimal purchaseAmount) {
        return discountService.calculateDiscount(id, purchaseAmount);
    }
}