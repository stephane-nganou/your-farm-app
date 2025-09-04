package de.farm.app.subscriptions;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
  public ResponseEntity<Subscription> create(@AuthenticationPrincipal String userId,
    @RequestBody CreateRequest req){
    var subscription = subscriptionService.create(UUID.fromString(userId), req.planName(),
        req.cadence(), req.priceCents(), req.currency());
        
    return ResponseEntity.ok(subscription);
  }
}
