package com.brightloop.finance.service;

import com.brightloop.finance.entity.Invoice;
import com.brightloop.finance.repository.InvoiceRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final InvoiceRepository invoiceRepository;

    public DashboardService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Map<String, Object> getDashboardSummary() {

        List<Invoice> invoices = invoiceRepository.findAll();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal outstandingAmount = BigDecimal.ZERO;

        long paidInvoices = 0;
        long pendingInvoices = 0;
        long overdueInvoices = 0;

        for (Invoice invoice : invoices) {

            if ("PAID".equalsIgnoreCase(invoice.getStatus())) {
                paidInvoices++;
                totalRevenue = totalRevenue.add(invoice.getTotalAmount());
            } else {
                pendingInvoices++;
                outstandingAmount = outstandingAmount.add(
                        invoice.getTotalAmount()
                );

                if (invoice.getDueDate() != null &&
                        invoice.getDueDate().isBefore(LocalDate.now())) {
                    overdueInvoices++;
                }
            }
        }

        Map<String, Object> summary = new HashMap<>();

        summary.put("totalInvoices", invoices.size());
        summary.put("totalRevenue", totalRevenue);
        summary.put("outstandingAmount", outstandingAmount);
        summary.put("paidInvoices", paidInvoices);
        summary.put("pendingInvoices", pendingInvoices);
        summary.put("overdueInvoices", overdueInvoices);

        return summary;
    }
}