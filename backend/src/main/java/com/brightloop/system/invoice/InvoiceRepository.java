package com.brightloop.system.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByCustomerId(Long customerId);

    List<Invoice> findByStatus(InvoiceStatus status);

    boolean existsBySubscriptionIdAndBillingPeriodStart(Long subscriptionId, LocalDate billingPeriodStart);

    long countByStatus(InvoiceStatus status);

    @Query("SELECT i FROM Invoice i ORDER BY i.id DESC")
    List<Invoice> findAllOrderByIdDesc();

    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :today AND i.status != 'PAID' AND i.status != 'CANCELLED'")
    List<Invoice> findOverdueInvoices(@Param("today") LocalDate today);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.status != 'CANCELLED'")
    BigDecimal sumTotalBilledRevenue();

    @Query("SELECT COALESCE(SUM(i.outstandingAmount), 0) FROM Invoice i WHERE i.status != 'PAID' AND i.status != 'CANCELLED'")
    BigDecimal sumTotalOutstanding();

    @Query("SELECT COALESCE(SUM(i.outstandingAmount), 0) FROM Invoice i WHERE i.dueDate < :today AND i.status != 'PAID' AND i.status != 'CANCELLED'")
    BigDecimal sumTotalOverdue(@Param("today") LocalDate today);
}
