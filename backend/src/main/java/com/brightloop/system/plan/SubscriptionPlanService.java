package com.brightloop.system.plan;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;

    public SubscriptionPlanService(SubscriptionPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public List<PlanDTO> getAllPlans() {
        return planRepository.findByActiveTrue().stream()
                .map(PlanDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public PlanDTO getPlanById(Long id) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription plan not found with id: " + id));
        return PlanDTO.fromEntity(plan);
    }

    @Transactional
    public PlanDTO createPlan(PlanDTO dto) {
        SubscriptionPlan plan = new SubscriptionPlan(
                dto.getPlanName(),
                dto.getBillingInterval(),
                dto.getIntervalCount(),
                dto.getBasePrice(),
                dto.getMinQuantity(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getDescription()
        );
        SubscriptionPlan saved = planRepository.save(plan);
        return PlanDTO.fromEntity(saved);
    }

    @Transactional
    public PlanDTO updatePlan(Long id, PlanDTO dto) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription plan not found with id: " + id));

        plan.setPlanName(dto.getPlanName());
        plan.setBillingInterval(dto.getBillingInterval());
        plan.setIntervalCount(dto.getIntervalCount());
        plan.setBasePrice(dto.getBasePrice());
        plan.setMinQuantity(dto.getMinQuantity());
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());
        plan.setDescription(dto.getDescription());
        plan.setActive(dto.isActive());

        SubscriptionPlan updated = planRepository.save(plan);
        return PlanDTO.fromEntity(updated);
    }

    @Transactional
    public void deletePlan(Long id) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription plan not found with id: " + id));
        plan.setActive(false);
        planRepository.save(plan);
    }
}
