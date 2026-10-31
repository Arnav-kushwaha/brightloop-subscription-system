package com.brightloop.system.subscription;

import com.brightloop.system.auth.User;
import com.brightloop.system.auth.UserRepository;
import com.brightloop.system.plan.BillingInterval;
import com.brightloop.system.plan.SubscriptionPlan;
import com.brightloop.system.plan.SubscriptionPlanRepository;
import com.brightloop.system.product.Product;
import com.brightloop.system.product.ProductRepository;
import com.brightloop.system.product.ProductVariant;
import com.brightloop.system.product.ProductVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               UserRepository userRepository,
                               SubscriptionPlanRepository planRepository,
                               ProductRepository productRepository,
                               ProductVariantRepository variantRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    public List<SubscriptionDTO> getAllSubscriptions() {
        return subscriptionRepository.findAllOrderByIdDesc().stream()
                .map(SubscriptionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public SubscriptionDTO getSubscriptionById(Long id) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with id: " + id));
        return SubscriptionDTO.fromEntity(sub);
    }

    public List<SubscriptionDTO> getSubscriptionsByCustomer(Long customerId) {
        return subscriptionRepository.findByCustomerId(customerId).stream()
                .map(SubscriptionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<SubscriptionDTO> getSubscriptionsByStatus(SubscriptionStatus status) {
        return subscriptionRepository.findByStatus(status).stream()
                .map(SubscriptionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubscriptionDTO createSubscription(CreateSubscriptionRequest request) {
        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + request.getCustomerId()));

        SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new IllegalArgumentException("Subscription plan not found with id: " + request.getPlanId()));

        if (request.getQuantity() < plan.getMinQuantity()) {
            throw new IllegalArgumentException("Minimum quantity for this plan is " + plan.getMinQuantity());
        }

        Product product = null;
        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId()).orElse(null);
        }

        ProductVariant variant = null;
        if (request.getVariantId() != null) {
            variant = variantRepository.findById(request.getVariantId()).orElse(null);
        }

        // Calculate unit price & recurring amount
        BigDecimal unitPrice = plan.getBasePrice();
        if (product != null) {
            unitPrice = unitPrice.add(product.getSellingPrice());
        }
        if (variant != null && variant.getAdditionalPrice() != null) {
            unitPrice = unitPrice.add(variant.getAdditionalPrice());
        }

        BigDecimal recurringAmount = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
        LocalDate expiryDate = calculateNextCycleDate(startDate, plan.getBillingInterval(), plan.getIntervalCount());
        LocalDate nextBillingDate = expiryDate;

        String subNumber = generateSubscriptionNumber();

        Subscription subscription = new Subscription(
                subNumber,
                customer,
                plan,
                product,
                variant,
                request.getQuantity(),
                unitPrice,
                recurringAmount,
                plan.getBillingInterval(),
                startDate,
                expiryDate,
                nextBillingDate,
                SubscriptionStatus.ACTIVE,
                null
        );

        Subscription saved = subscriptionRepository.save(subscription);
        return SubscriptionDTO.fromEntity(saved);
    }

    @Transactional
    public SubscriptionDTO pauseSubscription(Long id) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with id: " + id));

        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Only active subscriptions can be paused. Current status: " + sub.getStatus());
        }

        sub.setStatus(SubscriptionStatus.PAUSED);
        Subscription updated = subscriptionRepository.save(sub);
        return SubscriptionDTO.fromEntity(updated);
    }

    @Transactional
    public SubscriptionDTO resumeSubscription(Long id) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with id: " + id));

        if (sub.getStatus() != SubscriptionStatus.PAUSED) {
            throw new IllegalStateException("Only paused subscriptions can be resumed. Current status: " + sub.getStatus());
        }

        sub.setStatus(SubscriptionStatus.ACTIVE);
        // Extend next billing date if paused during the period
        if (sub.getNextBillingDate().isBefore(LocalDate.now())) {
            sub.setNextBillingDate(LocalDate.now().plusDays(7));
        }

        Subscription updated = subscriptionRepository.save(sub);
        return SubscriptionDTO.fromEntity(updated);
    }

    @Transactional
    public SubscriptionDTO renewSubscription(Long id, int cyclesToAdd) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with id: " + id));

        if (sub.getStatus() == SubscriptionStatus.CLOSED) {
            throw new IllegalStateException("Closed subscriptions cannot be renewed");
        }

        int cycles = cyclesToAdd > 0 ? cyclesToAdd : 1;
        LocalDate base = sub.getExpiryDate().isAfter(LocalDate.now()) ? sub.getExpiryDate() : LocalDate.now();
        LocalDate newExpiry = calculateNextCycleDate(base, sub.getBillingInterval(), cycles);

        sub.setExpiryDate(newExpiry);
        sub.setNextBillingDate(base);
        sub.setStatus(SubscriptionStatus.ACTIVE);

        Subscription updated = subscriptionRepository.save(sub);
        return SubscriptionDTO.fromEntity(updated);
    }

    @Transactional
    public SubscriptionDTO closeSubscription(Long id) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with id: " + id));

        sub.setStatus(SubscriptionStatus.CLOSED);
        Subscription updated = subscriptionRepository.save(sub);
        return SubscriptionDTO.fromEntity(updated);
    }

    public static LocalDate calculateNextCycleDate(LocalDate fromDate, BillingInterval interval, int count) {
        int intervals = Math.max(1, count);
        return switch (interval) {
            case DAILY -> fromDate.plusDays(intervals);
            case WEEKLY -> fromDate.plusWeeks(intervals);
            case MONTHLY -> fromDate.plusMonths(intervals);
            case YEARLY -> fromDate.plusYears(intervals);
        };
    }

    private String generateSubscriptionNumber() {
        return "SUB-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
