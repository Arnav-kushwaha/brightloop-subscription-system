package com.brightloop.finance.repository;

import com.brightloop.finance.entity.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuotationRepository
        extends JpaRepository<Quotation, Long> {

}