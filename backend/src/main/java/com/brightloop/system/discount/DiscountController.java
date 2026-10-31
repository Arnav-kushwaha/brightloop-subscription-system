package com.brightloop.system.discount;

import com.brightloop.system.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DiscountDTO>>> getAllDiscounts() {
        List<DiscountDTO> discounts = discountService.getAllDiscounts();
        return ResponseEntity.ok(ApiResponse.success(discounts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DiscountDTO>> getDiscountById(@PathVariable Long id) {
        DiscountDTO discount = discountService.getDiscountById(id);
        return ResponseEntity.ok(ApiResponse.success(discount));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DiscountDTO>> createDiscount(@Valid @RequestBody DiscountDTO dto) {
        DiscountDTO created = discountService.createDiscount(dto);
        return new ResponseEntity<>(ApiResponse.success(created, "Discount created successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DiscountDTO>> updateDiscount(@PathVariable Long id, @Valid @RequestBody DiscountDTO dto) {
        DiscountDTO updated = discountService.updateDiscount(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Discount updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDiscount(@PathVariable Long id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Discount deleted successfully"));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<DiscountValidationDTO.Response>> validateDiscount(
            @Valid @RequestBody DiscountValidationDTO.Request request
    ) {
        DiscountValidationDTO.Response response = discountService.validateDiscount(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
