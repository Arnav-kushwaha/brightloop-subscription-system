package com.brightloop.finance.service;

import com.brightloop.finance.entity.Discount;
import com.brightloop.finance.repository.DiscountRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class DiscountService {

    private final DiscountRepository discountRepository;

    public DiscountService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    public Discount createDiscount(Discount discount) {
        if (discount.getDiscountValue() == null ||
                discount.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid discount value");
        }

        if ("PERCENTAGE".equalsIgnoreCase(discount.getDiscountType()) &&
                discount.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentage cannot exceed 100");
        }

        if (discount.getStartDate() == null || discount.getEndDate() == null ||
                discount.getEndDate().isBefore(discount.getStartDate())) {
            throw new IllegalArgumentException("Invalid discount dates");
        }

        return discountRepository.save(discount);
    }

    public List<Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }

    public BigDecimal calculateDiscount(Long id, BigDecimal purchaseAmount) {

        Discount discount = discountRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Discount not found"));

        LocalDate today = LocalDate.now();

        if (!Boolean.TRUE.equals(discount.getActive()) ||
                today.isBefore(discount.getStartDate()) ||
                today.isAfter(discount.getEndDate())) {
            throw new IllegalArgumentException("Discount is not active");
        }

        if (purchaseAmount == null ||
                purchaseAmount.compareTo(discount.getMinimumPurchase()) < 0) {
            throw new IllegalArgumentException("Minimum purchase not met");
        }

        BigDecimal value = discount.getDiscountValue();

        if ("PERCENTAGE".equalsIgnoreCase(discount.getDiscountType())) {
            return purchaseAmount.multiply(value)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }

        if ("FIXED".equalsIgnoreCase(discount.getDiscountType())) {
            return value.min(purchaseAmount);
        }

        throw new IllegalArgumentException("Invalid discount type");
    }
}