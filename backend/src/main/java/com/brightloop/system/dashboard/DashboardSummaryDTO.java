package com.brightloop.system.dashboard;

import com.brightloop.system.invoice.InvoiceDTO;
import com.brightloop.system.payment.PaymentDTO;
import com.brightloop.system.subscription.SubscriptionDTO;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryDTO {

    private long activeSubscriptionsCount;
    private long totalSubscriptionsCount;
    private BigDecimal monthlyRecurringRevenue;
    private BigDecimal totalRevenueBilled;
    private BigDecimal totalPaymentsReceived;
    private BigDecimal totalOutstanding;
    private BigDecimal totalOverdue;
    private long overdueInvoicesCount;
    private long paidInvoicesCount;
    private List<SubscriptionDTO> recentSubscriptions;
    private List<InvoiceDTO> recentInvoices;
    private List<PaymentDTO> recentPayments;

    public DashboardSummaryDTO() {
    }

    public long getActiveSubscriptionsCount() {
        return activeSubscriptionsCount;
    }

    public void setActiveSubscriptionsCount(long activeSubscriptionsCount) {
        this.activeSubscriptionsCount = activeSubscriptionsCount;
    }

    public long getTotalSubscriptionsCount() {
        return totalSubscriptionsCount;
    }

    public void setTotalSubscriptionsCount(long totalSubscriptionsCount) {
        this.totalSubscriptionsCount = totalSubscriptionsCount;
    }

    public BigDecimal getMonthlyRecurringRevenue() {
        return monthlyRecurringRevenue;
    }

    public void setMonthlyRecurringRevenue(BigDecimal monthlyRecurringRevenue) {
        this.monthlyRecurringRevenue = monthlyRecurringRevenue;
    }

    public BigDecimal getTotalRevenueBilled() {
        return totalRevenueBilled;
    }

    public void setTotalRevenueBilled(BigDecimal totalRevenueBilled) {
        this.totalRevenueBilled = totalRevenueBilled;
    }

    public BigDecimal getTotalPaymentsReceived() {
        return totalPaymentsReceived;
    }

    public void setTotalPaymentsReceived(BigDecimal totalPaymentsReceived) {
        this.totalPaymentsReceived = totalPaymentsReceived;
    }

    public BigDecimal getTotalOutstanding() {
        return totalOutstanding;
    }

    public void setTotalOutstanding(BigDecimal totalOutstanding) {
        this.totalOutstanding = totalOutstanding;
    }

    public BigDecimal getTotalOverdue() {
        return totalOverdue;
    }

    public void setTotalOverdue(BigDecimal totalOverdue) {
        this.totalOverdue = totalOverdue;
    }

    public long getOverdueInvoicesCount() {
        return overdueInvoicesCount;
    }

    public void setOverdueInvoicesCount(long overdueInvoicesCount) {
        this.overdueInvoicesCount = overdueInvoicesCount;
    }

    public long getPaidInvoicesCount() {
        return paidInvoicesCount;
    }

    public void setPaidInvoicesCount(long paidInvoicesCount) {
        this.paidInvoicesCount = paidInvoicesCount;
    }

    public List<SubscriptionDTO> getRecentSubscriptions() {
        return recentSubscriptions;
    }

    public void setRecentSubscriptions(List<SubscriptionDTO> recentSubscriptions) {
        this.recentSubscriptions = recentSubscriptions;
    }

    public List<InvoiceDTO> getRecentInvoices() {
        return recentInvoices;
    }

    public void setRecentInvoices(List<InvoiceDTO> recentInvoices) {
        this.recentInvoices = recentInvoices;
    }

    public List<PaymentDTO> getRecentPayments() {
        return recentPayments;
    }

    public void setRecentPayments(List<PaymentDTO> recentPayments) {
        this.recentPayments = recentPayments;
    }
}
