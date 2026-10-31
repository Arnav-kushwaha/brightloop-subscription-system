package com.brightloop.system.subscription;

import com.brightloop.system.auth.Role;
import com.brightloop.system.auth.User;
import com.brightloop.system.auth.UserRepository;
import com.brightloop.system.plan.BillingInterval;
import com.brightloop.system.plan.SubscriptionPlan;
import com.brightloop.system.plan.SubscriptionPlanRepository;
import com.brightloop.system.product.ProductRepository;
import com.brightloop.system.product.ProductVariantRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionLifecycleTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionPlanRepository planRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository variantRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User customer;
    private SubscriptionPlan plan;
    private Subscription activeSubscription;

    @BeforeEach
    void setUp() {
        customer = new User("Jane Doe", "jane@example.com", "hash", Role.CUSTOMER);
        customer.setId(10L);

        plan = new SubscriptionPlan("Pro Monthly", BillingInterval.MONTHLY, 1, new BigDecimal("49.99"), 1, LocalDate.now(), null, "Test Plan");
        plan.setId(20L);

        activeSubscription = new Subscription(
                "SUB-2026-TEST01",
                customer,
                plan,
                null,
                null,
                1,
                new BigDecimal("49.99"),
                new BigDecimal("49.99"),
                BillingInterval.MONTHLY,
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                LocalDate.now().plusMonths(1),
                SubscriptionStatus.ACTIVE,
                null
        );
        activeSubscription.setId(100L);
    }

    @Test
    void testPauseActiveSubscription_Success() {
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(activeSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        SubscriptionDTO dto = subscriptionService.pauseSubscription(100L);

        assertNotNull(dto);
        assertEquals(SubscriptionStatus.PAUSED, dto.getStatus());
    }

    @Test
    void testPauseAlreadyPausedSubscription_ThrowsIllegalStateException() {
        activeSubscription.setStatus(SubscriptionStatus.PAUSED);
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(activeSubscription));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                subscriptionService.pauseSubscription(100L)
        );
        assertTrue(ex.getMessage().contains("Only active subscriptions can be paused"));
    }

    @Test
    void testResumePausedSubscription_Success() {
        activeSubscription.setStatus(SubscriptionStatus.PAUSED);
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(activeSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        SubscriptionDTO dto = subscriptionService.resumeSubscription(100L);

        assertNotNull(dto);
        assertEquals(SubscriptionStatus.ACTIVE, dto.getStatus());
    }

    @Test
    void testRenewSubscription_Success() {
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(activeSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        SubscriptionDTO dto = subscriptionService.renewSubscription(100L, 2);

        assertNotNull(dto);
        assertEquals(SubscriptionStatus.ACTIVE, dto.getStatus());
        assertTrue(dto.getExpiryDate().isAfter(LocalDate.now().plusMonths(1)));
    }

    @Test
    void testCloseSubscription_Success() {
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(activeSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        SubscriptionDTO dto = subscriptionService.closeSubscription(100L);

        assertNotNull(dto);
        assertEquals(SubscriptionStatus.CLOSED, dto.getStatus());
    }

    @Test
    void testRenewClosedSubscription_ThrowsIllegalStateException() {
        activeSubscription.setStatus(SubscriptionStatus.CLOSED);
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(activeSubscription));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                subscriptionService.renewSubscription(100L, 1)
        );
        assertTrue(ex.getMessage().contains("Closed subscriptions cannot be renewed"));
    }
}
