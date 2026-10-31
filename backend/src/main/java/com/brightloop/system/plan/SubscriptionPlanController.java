package com.brightloop.system.plan;

import com.brightloop.system.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class SubscriptionPlanController {

    private final SubscriptionPlanService planService;

    public SubscriptionPlanController(SubscriptionPlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlanDTO>>> getAllPlans() {
        List<PlanDTO> plans = planService.getAllPlans();
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlanDTO>> getPlanById(@PathVariable Long id) {
        PlanDTO plan = planService.getPlanById(id);
        return ResponseEntity.ok(ApiResponse.success(plan));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PlanDTO>> createPlan(@Valid @RequestBody PlanDTO dto) {
        PlanDTO created = planService.createPlan(dto);
        return new ResponseEntity<>(ApiResponse.success(created, "Subscription plan created successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PlanDTO>> updatePlan(@PathVariable Long id, @Valid @RequestBody PlanDTO dto) {
        PlanDTO updated = planService.updatePlan(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Subscription plan updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable Long id) {
        planService.deletePlan(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Subscription plan deleted successfully"));
    }
}
