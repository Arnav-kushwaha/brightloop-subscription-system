package com.brightloop.system.invoice;

import com.brightloop.system.auth.User;
import com.brightloop.system.auth.UserRepository;
import com.brightloop.system.subscription.Subscription;
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
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          UserRepository userRepository,
                          SubscriptionRepository subscriptionRepository) {
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(readOnly = true)
    public List<InvoiceDTO> getAllInvoices() {
        updateOverdueStatuses();
        return invoiceRepository.findAllOrderByIdDesc().stream()
                .map(InvoiceDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InvoiceDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with id: " + id));
        return InvoiceDTO.fromEntity(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceDTO> getInvoicesByCustomer(Long customerId) {
        return invoiceRepository.findByCustomerId(customerId).stream()
                .map(InvoiceDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvoiceDTO> getInvoicesByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status).stream()
                .map(InvoiceDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public InvoiceDTO createInvoice(CreateInvoiceRequest request) {
        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + request.getCustomerId()));

        Subscription subscription = null;
        if (request.getSubscriptionId() != null) {
            subscription = subscriptionRepository.findById(request.getSubscriptionId()).orElse(null);
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setCustomer(customer);
        invoice.setSubscription(subscription);
        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(request.getDueDate() != null ? request.getDueDate() : LocalDate.now().plusDays(15));
        invoice.setStatus(InvoiceStatus.CONFIRMED);
        invoice.setNotes(request.getNotes());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CreateInvoiceRequest.ItemRequest itemReq : request.getItems()) {
            int qty = Math.max(1, itemReq.getQuantity());
            BigDecimal unitPrice = itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
            subtotal = subtotal.add(lineTotal);

            InvoiceItem item = new InvoiceItem(invoice, itemReq.getDescription(), qty, unitPrice, lineTotal);
            invoice.addItem(item);
        }

        invoice.setSubtotal(subtotal);
        BigDecimal discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        invoice.setDiscountAmount(discount);

        BigDecimal taxableAmount = subtotal.subtract(discount);
        if (taxableAmount.compareTo(BigDecimal.ZERO) < 0) {
            taxableAmount = BigDecimal.ZERO;
        }

        BigDecimal taxRate = new BigDecimal("10.00");
        BigDecimal taxAmount = taxableAmount.multiply(taxRate).divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP);
        invoice.setTaxRate(taxRate);
        invoice.setTaxAmount(taxAmount);

        BigDecimal total = taxableAmount.add(taxAmount);
        invoice.setTotalAmount(total);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setOutstandingAmount(total);

        Invoice saved = invoiceRepository.save(invoice);
        return InvoiceDTO.fromEntity(saved);
    }

    @Transactional
    public InvoiceDTO confirmInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with id: " + id));

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT invoices can be confirmed");
        }

        invoice.setStatus(InvoiceStatus.CONFIRMED);
        Invoice saved = invoiceRepository.save(invoice);
        return InvoiceDTO.fromEntity(saved);
    }

    @Transactional
    public InvoiceDTO cancelInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with id: " + id));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Paid invoices cannot be cancelled");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        Invoice saved = invoiceRepository.save(invoice);
        return InvoiceDTO.fromEntity(saved);
    }

    /**
     * Automated recurring billing generation engine.
     * Prevents duplicate invoices and advances next billing dates.
     */
    @Transactional
    public List<InvoiceDTO> generateRecurringInvoices() {
        LocalDate today = LocalDate.now();
        List<Subscription> dueSubscriptions = subscriptionRepository.findByStatusAndNextBillingDateLessThanEqual(
                SubscriptionStatus.ACTIVE, today
        );

        List<InvoiceDTO> generated = new ArrayList<>();

        for (Subscription sub : dueSubscriptions) {
            LocalDate cycleStart = sub.getNextBillingDate();
            LocalDate cycleEnd = SubscriptionService.calculateNextCycleDate(cycleStart, sub.getBillingInterval(), sub.getPlan().getIntervalCount());

            // Duplicate prevention check
            boolean alreadyGenerated = invoiceRepository.existsBySubscriptionIdAndBillingPeriodStart(sub.getId(), cycleStart);
            if (alreadyGenerated) {
                // Advance billing date to prevent stuck loops
                sub.setNextBillingDate(cycleEnd);
                subscriptionRepository.save(sub);
                continue;
            }

            Invoice invoice = new Invoice();
            invoice.setInvoiceNumber(generateInvoiceNumber());
            invoice.setSubscription(sub);
            invoice.setCustomer(sub.getCustomer());
            invoice.setIssueDate(today);
            invoice.setDueDate(today.plusDays(15));
            invoice.setStatus(InvoiceStatus.CONFIRMED);
            invoice.setBillingPeriodStart(cycleStart);
            invoice.setBillingPeriodEnd(cycleEnd);
            invoice.setNotes("Recurring billing for subscription " + sub.getSubscriptionNumber() +
                    " (" + sub.getBillingInterval() + " cycle)");

            String description = sub.getPlan().getPlanName();
            if (sub.getProduct() != null) {
                description += " - " + sub.getProduct().getName();
            }
            if (sub.getVariant() != null) {
                description += " (" + sub.getVariant().getVariantName() + ")";
            }

            BigDecimal unitPrice = sub.getUnitPrice();
            int qty = sub.getQuantity();
            BigDecimal subtotal = sub.getRecurringAmount();

            InvoiceItem item = new InvoiceItem(invoice, description, qty, unitPrice, subtotal);
            invoice.addItem(item);

            invoice.setSubtotal(subtotal);
            invoice.setDiscountAmount(BigDecimal.ZERO);

            BigDecimal taxRate = new BigDecimal("10.00");
            BigDecimal taxAmount = subtotal.multiply(taxRate).divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP);
            invoice.setTaxRate(taxRate);
            invoice.setTaxAmount(taxAmount);

            BigDecimal total = subtotal.add(taxAmount);
            invoice.setTotalAmount(total);
            invoice.setPaidAmount(BigDecimal.ZERO);
            invoice.setOutstandingAmount(total);

            Invoice savedInvoice = invoiceRepository.save(invoice);
            generated.add(InvoiceDTO.fromEntity(savedInvoice));

            // Advance subscription next billing date
            sub.setNextBillingDate(cycleEnd);
            subscriptionRepository.save(sub);
        }

        return generated;
    }

    @Transactional
    public void updateOverdueStatuses() {
        LocalDate today = LocalDate.now();
        List<Invoice> overdue = invoiceRepository.findOverdueInvoices(today);
        for (Invoice inv : overdue) {
            if (inv.getStatus() == InvoiceStatus.CONFIRMED) {
                inv.setStatus(InvoiceStatus.OVERDUE);
                invoiceRepository.save(inv);
            }
        }
    }

    private String generateInvoiceNumber() {
        return "INV-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
