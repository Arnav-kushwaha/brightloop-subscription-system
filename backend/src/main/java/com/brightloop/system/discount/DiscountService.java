package com.brightloop.system.discount;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DiscountService {

    private final DiscountRepository discountRepository;

    public DiscountService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    public List<DiscountDTO> getAllDiscounts() {
        return discountRepository.findAll().stream()
                .map(DiscountDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public DiscountDTO getDiscountById(Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discount not found with id: " + id));
        return DiscountDTO.fromEntity(discount);
    }

    @Transactional
    public DiscountDTO createDiscount(DiscountDTO dto) {
        String code = dto.getCode().trim().toUpperCase();
        if (discountRepository.findByCodeIgnoreCase(code).isPresent()) {
            throw new IllegalArgumentException("Discount code already exists: " + code);
        }

        Discount discount = new Discount(
                code,
                dto.getType(),
                dto.getAmount(),
                dto.getMinPurchaseAmount(),
                dto.getMinQuantity(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getUsageLimit()
        );

        Discount saved = discountRepository.save(discount);
        return DiscountDTO.fromEntity(saved);
    }

    @Transactional
    public DiscountDTO updateDiscount(Long id, DiscountDTO dto) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discount not found with id: " + id));

        discount.setType(dto.getType());
        discount.setAmount(dto.getAmount());
        discount.setMinPurchaseAmount(dto.getMinPurchaseAmount());
        discount.setMinQuantity(dto.getMinQuantity());
        discount.setStartDate(dto.getStartDate());
        discount.setEndDate(dto.getEndDate());
        discount.setUsageLimit(dto.getUsageLimit());
        discount.setActive(dto.isActive());

        Discount updated = discountRepository.save(discount);
        return DiscountDTO.fromEntity(updated);
    }

    @Transactional
    public void deleteDiscount(Long id) {
        discountRepository.deleteById(id);
    }

    public DiscountValidationDTO.Response validateDiscount(DiscountValidationDTO.Request request) {
        String code = request.getCode().trim().toUpperCase();
        Optional<Discount> opt = discountRepository.findByCodeIgnoreCase(code);

        if (opt.isEmpty()) {
            return new DiscountValidationDTO.Response(false, "Invalid coupon code", code, BigDecimal.ZERO, request.getPurchaseAmount());
        }

        Discount discount = opt.get();
        LocalDate today = LocalDate.now();

        if (!discount.isActive()) {
            return new DiscountValidationDTO.Response(false, "This coupon is currently inactive", code, BigDecimal.ZERO, request.getPurchaseAmount());
        }

        if (today.isBefore(discount.getStartDate())) {
            return new DiscountValidationDTO.Response(false, "This coupon is not active yet", code, BigDecimal.ZERO, request.getPurchaseAmount());
        }

        if (today.isAfter(discount.getEndDate())) {
            return new DiscountValidationDTO.Response(false, "This coupon has expired", code, BigDecimal.ZERO, request.getPurchaseAmount());
        }

        if (discount.getTimesUsed() >= discount.getUsageLimit()) {
            return new DiscountValidationDTO.Response(false, "This coupon has reached its maximum usage limit", code, BigDecimal.ZERO, request.getPurchaseAmount());
        }

        if (discount.getMinPurchaseAmount() != null && request.getPurchaseAmount().compareTo(discount.getMinPurchaseAmount()) < 0) {
            return new DiscountValidationDTO.Response(false, "Minimum purchase amount of $" + discount.getMinPurchaseAmount() + " required", code, BigDecimal.ZERO, request.getPurchaseAmount());
        }

        if (request.getQuantity() < discount.getMinQuantity()) {
            return new DiscountValidationDTO.Response(false, "Minimum quantity of " + discount.getMinQuantity() + " required", code, BigDecimal.ZERO, request.getPurchaseAmount());
        }

        BigDecimal discountAmount;
        if (discount.getType() == DiscountType.PERCENTAGE) {
            discountAmount = request.getPurchaseAmount()
                    .multiply(discount.getAmount())
                    .divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP);
        } else {
            discountAmount = discount.getAmount();
        }

        if (discountAmount.compareTo(request.getPurchaseAmount()) > 0) {
            discountAmount = request.getPurchaseAmount();
        }

        BigDecimal finalAmount = request.getPurchaseAmount().subtract(discountAmount);
        return new DiscountValidationDTO.Response(true, "Coupon applied successfully!", code, discountAmount, finalAmount);
    }

    @Transactional
    public void incrementUsage(String code) {
        discountRepository.findByCodeIgnoreCase(code).ifPresent(d -> {
            d.setTimesUsed(d.getTimesUsed() + 1);
            discountRepository.save(d);
        });
    }
}
