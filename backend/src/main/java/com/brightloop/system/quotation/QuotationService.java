package com.brightloop.system.quotation;

import com.brightloop.system.auth.User;
import com.brightloop.system.auth.UserRepository;
import com.brightloop.system.plan.SubscriptionPlan;
import com.brightloop.system.plan.SubscriptionPlanRepository;
import com.brightloop.system.product.Product;
import com.brightloop.system.product.ProductRepository;
import com.brightloop.system.product.ProductVariant;
import com.brightloop.system.product.ProductVariantRepository;
import com.brightloop.system.subscription.Subscription;
import com.brightloop.system.subscription.SubscriptionDTO;
import com.brightloop.system.subscription.SubscriptionRepository;
import com.brightloop.system.subscription.SubscriptionService;
import com.brightloop.system.subscription.SubscriptionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuotationService {

    private final QuotationRepository quotationRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final SubscriptionRepository subscriptionRepository;

    public QuotationService(QuotationRepository quotationRepository,
                            UserRepository userRepository,
                            SubscriptionPlanRepository planRepository,
                            ProductRepository productRepository,
                            ProductVariantRepository variantRepository,
                            SubscriptionRepository subscriptionRepository) {
        this.quotationRepository = quotationRepository;
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public List<QuotationDTO> getAllQuotations() {
        return quotationRepository.findAllOrderByIdDesc().stream()
                .map(QuotationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public QuotationDTO getQuotationById(Long id) {
        Quotation quote = quotationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found with id: " + id));
        return QuotationDTO.fromEntity(quote);
    }

    @Transactional
    public QuotationDTO createQuotation(CreateQuotationRequest request) {
        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + request.getCustomerId()));

        Quotation quotation = new Quotation();
        quotation.setQuotationNumber(generateQuotationNumber());
        quotation.setCustomer(customer);
        quotation.setValidUntil(request.getValidUntil());
        quotation.setStatus(QuotationStatus.DRAFT);
        quotation.setNotes(request.getNotes());

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CreateQuotationRequest.ItemRequest itemReq : request.getItems()) {
            SubscriptionPlan plan = planRepository.findById(itemReq.getPlanId())
                    .orElseThrow(() -> new IllegalArgumentException("Plan not found with id: " + itemReq.getPlanId()));

            Product product = null;
            if (itemReq.getProductId() != null) {
                product = productRepository.findById(itemReq.getProductId()).orElse(null);
            }

            ProductVariant variant = null;
            if (itemReq.getVariantId() != null) {
                variant = variantRepository.findById(itemReq.getVariantId()).orElse(null);
            }

            BigDecimal unitPrice = plan.getBasePrice();
            if (product != null) {
                unitPrice = unitPrice.add(product.getSellingPrice());
            }
            if (variant != null && variant.getAdditionalPrice() != null) {
                unitPrice = unitPrice.add(variant.getAdditionalPrice());
            }

            int qty = Math.max(1, itemReq.getQuantity());
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
            subtotal = subtotal.add(lineTotal);

            QuotationItem item = new QuotationItem(quotation, product, variant, plan, qty, unitPrice, lineTotal);
            quotation.addItem(item);
        }

        quotation.setSubtotal(subtotal);
        BigDecimal discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        quotation.setDiscountAmount(discount);

        BigDecimal taxableAmount = subtotal.subtract(discount);
        if (taxableAmount.compareTo(BigDecimal.ZERO) < 0) {
            taxableAmount = BigDecimal.ZERO;
        }

        BigDecimal taxRate = new BigDecimal("10.00"); // 10%
        BigDecimal taxAmount = taxableAmount.multiply(taxRate).divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP);
        quotation.setTaxRate(taxRate);
        quotation.setTaxAmount(taxAmount);

        BigDecimal total = taxableAmount.add(taxAmount);
        quotation.setTotalAmount(total);

        Quotation saved = quotationRepository.save(quotation);
        return QuotationDTO.fromEntity(saved);
    }

    @Transactional
    public QuotationDTO confirmQuotation(Long id) {
        Quotation quote = quotationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found with id: " + id));

        if (quote.getStatus() == QuotationStatus.EXPIRED || quote.getStatus() == QuotationStatus.CONVERTED) {
            throw new IllegalStateException("Quotation cannot be confirmed in current status: " + quote.getStatus());
        }

        quote.setStatus(QuotationStatus.CONFIRMED);
        Quotation updated = quotationRepository.save(quote);
        return QuotationDTO.fromEntity(updated);
    }

    /**
     * Converts a quotation into one or more live subscriptions.
     */
    @Transactional
    public List<SubscriptionDTO> convertToSubscription(Long quotationId) {
        Quotation quote = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found with id: " + quotationId));

        if (quote.getStatus() == QuotationStatus.CONVERTED) {
            throw new IllegalStateException("This quotation has already been converted to a subscription");
        }

        List<SubscriptionDTO> createdSubscriptions = new ArrayList<>();

        for (QuotationItem item : quote.getItems()) {
            SubscriptionPlan plan = item.getPlan();
            LocalDate startDate = LocalDate.now();
            LocalDate expiryDate = SubscriptionService.calculateNextCycleDate(startDate, plan.getBillingInterval(), plan.getIntervalCount());

            String subNumber = "SUB-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            Subscription subscription = new Subscription(
                    subNumber,
                    quote.getCustomer(),
                    plan,
                    item.getProduct(),
                    item.getVariant(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getLineTotal(),
                    plan.getBillingInterval(),
                    startDate,
                    expiryDate,
                    expiryDate,
                    SubscriptionStatus.ACTIVE,
                    quote.getId()
            );

            Subscription saved = subscriptionRepository.save(subscription);
            createdSubscriptions.add(SubscriptionDTO.fromEntity(saved));
        }

        quote.setStatus(QuotationStatus.CONVERTED);
        quotationRepository.save(quote);

        return createdSubscriptions;
    }

    private String generateQuotationNumber() {
        return "QUO-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
