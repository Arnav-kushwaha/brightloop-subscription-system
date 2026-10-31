package com.brightloop.system.discount;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscountCalculationTest {

    @Mock
    private DiscountRepository discountRepository;

    @InjectMocks
    private DiscountService discountService;

    private Discount percentageDiscount;
    private Discount fixedDiscount;

    @BeforeEach
    void setUp() {
        percentageDiscount = new Discount(
                "FLUX20",
                DiscountType.PERCENTAGE,
                new BigDecimal("20.00"),
                new BigDecimal("50.00"),
                1,
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(30),
                100
        );

        fixedDiscount = new Discount(
                "SAVE15",
                DiscountType.FIXED,
                new BigDecimal("15.00"),
                new BigDecimal("30.00"),
                1,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(10),
                50
        );
    }

    @Test
    void testPercentageDiscount_AppliedCorrectly() {
        when(discountRepository.findByCodeIgnoreCase("FLUX20")).thenReturn(Optional.of(percentageDiscount));

        DiscountValidationDTO.Request request = new DiscountValidationDTO.Request("FLUX20", new BigDecimal("100.00"), 1);
        DiscountValidationDTO.Response response = discountService.validateDiscount(request);

        assertTrue(response.isValid());
        assertEquals(new BigDecimal("20.00"), response.getDiscountAmount());
        assertEquals(new BigDecimal("80.00"), response.getFinalAmount());
    }

    @Test
    void testFixedDiscount_AppliedCorrectly() {
        when(discountRepository.findByCodeIgnoreCase("SAVE15")).thenReturn(Optional.of(fixedDiscount));

        DiscountValidationDTO.Request request = new DiscountValidationDTO.Request("SAVE15", new BigDecimal("50.00"), 1);
        DiscountValidationDTO.Response response = discountService.validateDiscount(request);

        assertTrue(response.isValid());
        assertEquals(new BigDecimal("15.00"), response.getDiscountAmount());
        assertEquals(new BigDecimal("35.00"), response.getFinalAmount());
    }

    @Test
    void testMinPurchaseNotMet_Fails() {
        when(discountRepository.findByCodeIgnoreCase("FLUX20")).thenReturn(Optional.of(percentageDiscount));

        // Minimum is $50.00, purchase is $40.00
        DiscountValidationDTO.Request request = new DiscountValidationDTO.Request("FLUX20", new BigDecimal("40.00"), 1);
        DiscountValidationDTO.Response response = discountService.validateDiscount(request);

        assertFalse(response.isValid());
        assertTrue(response.getMessage().contains("Minimum purchase amount"));
        assertEquals(BigDecimal.ZERO, response.getDiscountAmount());
    }

    @Test
    void testExpiredDiscount_Fails() {
        percentageDiscount.setEndDate(LocalDate.now().minusDays(1));
        when(discountRepository.findByCodeIgnoreCase("FLUX20")).thenReturn(Optional.of(percentageDiscount));

        DiscountValidationDTO.Request request = new DiscountValidationDTO.Request("FLUX20", new BigDecimal("100.00"), 1);
        DiscountValidationDTO.Response response = discountService.validateDiscount(request);

        assertFalse(response.isValid());
        assertTrue(response.getMessage().contains("expired"));
    }

    @Test
    void testUsageLimitReached_Fails() {
        percentageDiscount.setTimesUsed(100);
        when(discountRepository.findByCodeIgnoreCase("FLUX20")).thenReturn(Optional.of(percentageDiscount));

        DiscountValidationDTO.Request request = new DiscountValidationDTO.Request("FLUX20", new BigDecimal("100.00"), 1);
        DiscountValidationDTO.Response response = discountService.validateDiscount(request);

        assertFalse(response.isValid());
        assertTrue(response.getMessage().contains("maximum usage limit"));
    }

    @Test
    void testDiscountExceedingPurchaseAmount_ClampedToPurchaseAmount() {
        fixedDiscount.setAmount(new BigDecimal("100.00"));
        fixedDiscount.setMinPurchaseAmount(new BigDecimal("10.00"));
        when(discountRepository.findByCodeIgnoreCase("SAVE15")).thenReturn(Optional.of(fixedDiscount));

        DiscountValidationDTO.Request request = new DiscountValidationDTO.Request("SAVE15", new BigDecimal("60.00"), 1);
        DiscountValidationDTO.Response response = discountService.validateDiscount(request);

        assertTrue(response.isValid());
        assertEquals(new BigDecimal("60.00"), response.getDiscountAmount());
        assertEquals(0, response.getFinalAmount().compareTo(BigDecimal.ZERO));
    }
}
