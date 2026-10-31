package com.brightloop.system.dashboard;

import com.brightloop.system.invoice.InvoiceDTO;
import com.brightloop.system.invoice.InvoiceRepository;
import com.brightloop.system.invoice.InvoiceService;
import com.brightloop.system.invoice.InvoiceStatus;
import com.brightloop.system.payment.PaymentDTO;
import com.brightloop.system.payment.PaymentRepository;
import com.brightloop.system.subscription.Subscription;
import com.brightloop.system.subscription.SubscriptionDTO;
import com.brightloop.system.subscription.SubscriptionRepository;
import com.brightloop.system.subscription.SubscriptionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;

    public DashboardService(SubscriptionRepository subscriptionRepository,
                            InvoiceRepository invoiceRepository,
                            PaymentRepository paymentRepository,
                            InvoiceService invoiceService) {
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.invoiceService = invoiceService;
    }

    @Transactional
    public DashboardSummaryDTO getSummary() {
        invoiceService.updateOverdueStatuses();
        LocalDate today = LocalDate.now();

        DashboardSummaryDTO summary = new DashboardSummaryDTO();

        long activeSubs = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        long totalSubs = subscriptionRepository.count();
        summary.setActiveSubscriptionsCount(activeSubs);
        summary.setTotalSubscriptionsCount(totalSubs);

        // Calculate MRR from active subscriptions
        List<Subscription> activeList = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);
        BigDecimal mrr = BigDecimal.ZERO;
        for (Subscription sub : activeList) {
            BigDecimal subMrr = switch (sub.getBillingInterval()) {
                case DAILY -> sub.getRecurringAmount().multiply(new BigDecimal("30"));
                case WEEKLY -> sub.getRecurringAmount().multiply(new BigDecimal("4.33")).setScale(2, RoundingMode.HALF_UP);
                case MONTHLY -> sub.getRecurringAmount();
                case YEARLY -> sub.getRecurringAmount().divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
            };
            mrr = mrr.add(subMrr);
        }
        summary.setMonthlyRecurringRevenue(mrr);

        // Revenue metrics from real MySQL database
        BigDecimal totalBilled = invoiceRepository.sumTotalBilledRevenue();
        BigDecimal totalPayments = paymentRepository.sumTotalPaymentsReceived();
        BigDecimal totalOutstanding = invoiceRepository.sumTotalOutstanding();
        BigDecimal totalOverdue = invoiceRepository.sumTotalOverdue(today);

        summary.setTotalRevenueBilled(totalBilled != null ? totalBilled : BigDecimal.ZERO);
        summary.setTotalPaymentsReceived(totalPayments != null ? totalPayments : BigDecimal.ZERO);
        summary.setTotalOutstanding(totalOutstanding != null ? totalOutstanding : BigDecimal.ZERO);
        summary.setTotalOverdue(totalOverdue != null ? totalOverdue : BigDecimal.ZERO);

        summary.setOverdueInvoicesCount(invoiceRepository.countByStatus(InvoiceStatus.OVERDUE));
        summary.setPaidInvoicesCount(invoiceRepository.countByStatus(InvoiceStatus.PAID));

        // Recent records (limit 5)
        List<SubscriptionDTO> recentSubs = subscriptionRepository.findAllOrderByIdDesc().stream()
                .limit(5)
                .map(SubscriptionDTO::fromEntity)
                .collect(Collectors.toList());
        summary.setRecentSubscriptions(recentSubs);

        List<InvoiceDTO> recentInvoices = invoiceRepository.findAllOrderByIdDesc().stream()
                .limit(5)
                .map(InvoiceDTO::fromEntity)
                .collect(Collectors.toList());
        summary.setRecentInvoices(recentInvoices);

        List<PaymentDTO> recentPayments = paymentRepository.findAllOrderByIdDesc().stream()
                .limit(5)
                .map(PaymentDTO::fromEntity)
                .collect(Collectors.toList());
        summary.setRecentPayments(recentPayments);

        return summary;
    }
}
