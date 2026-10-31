package com.brightloop.system.quotation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    Optional<Quotation> findByQuotationNumber(String quotationNumber);

    List<Quotation> findByCustomerId(Long customerId);

    List<Quotation> findByStatus(QuotationStatus status);

    @Query("SELECT q FROM Quotation q ORDER BY q.id DESC")
    List<Quotation> findAllOrderByIdDesc();
}
