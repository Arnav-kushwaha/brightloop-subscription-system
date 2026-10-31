package com.brightloop.system.subscription;

import com.brightloop.system.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionDTO>>> getAllSubscriptions(
            @RequestParam(required = false) SubscriptionStatus status,
            @RequestParam(required = false) Long customerId
    ) {
        List<SubscriptionDTO> subscriptions;
        if (status != null) {
            subscriptions = subscriptionService.getSubscriptionsByStatus(status);
        } else if (customerId != null) {
            subscriptions = subscriptionService.getSubscriptionsByCustomer(customerId);
        } else {
            subscriptions = subscriptionService.getAllSubscriptions();
        }
        return ResponseEntity.ok(ApiResponse.success(subscriptions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionDTO>> getSubscriptionById(@PathVariable Long id) {
        SubscriptionDTO subscription = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(ApiResponse.success(subscription));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionDTO>> createSubscription(@Valid @RequestBody CreateSubscriptionRequest request) {
        SubscriptionDTO created = subscriptionService.createSubscription(request);
        return new ResponseEntity<>(ApiResponse.success(created, "Subscription created successfully"), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/pause")
    public ResponseEntity<ApiResponse<SubscriptionDTO>> pauseSubscription(@PathVariable Long id) {
        SubscriptionDTO updated = subscriptionService.pauseSubscription(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Subscription paused successfully"));
    }

    @PatchMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<SubscriptionDTO>> resumeSubscription(@PathVariable Long id) {
        SubscriptionDTO updated = subscriptionService.resumeSubscription(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Subscription resumed successfully"));
    }

    @PatchMapping("/{id}/renew")
    public ResponseEntity<ApiResponse<SubscriptionDTO>> renewSubscription(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int cycles
    ) {
        SubscriptionDTO updated = subscriptionService.renewSubscription(id, cycles);
        return ResponseEntity.ok(ApiResponse.success(updated, "Subscription renewed successfully"));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<ApiResponse<SubscriptionDTO>> closeSubscription(@PathVariable Long id) {
        SubscriptionDTO updated = subscriptionService.closeSubscription(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Subscription closed successfully"));
    }
}
