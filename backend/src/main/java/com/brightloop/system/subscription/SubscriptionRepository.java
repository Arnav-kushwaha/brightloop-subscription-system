package com.brightloop.system.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findBySubscriptionNumber(String subscriptionNumber);

    List<Subscription> findByStatus(SubscriptionStatus status);

    List<Subscription> findByCustomerId(Long customerId);

    List<Subscription> findByStatusAndNextBillingDateLessThanEqual(SubscriptionStatus status, LocalDate date);

    long countByStatus(SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s ORDER BY s.id DESC")
    List<Subscription> findAllOrderByIdDesc();
}
