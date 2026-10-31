package com.brightloop.system.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoiceId(Long invoiceId);

    Optional<Payment> findByPaymentReference(String paymentReference);

    @Query("SELECT p FROM Payment p ORDER BY p.id DESC")
    List<Payment> findAllOrderByIdDesc();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p")
    BigDecimal sumTotalPaymentsReceived();
}
