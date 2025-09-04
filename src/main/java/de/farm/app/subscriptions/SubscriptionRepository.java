package de.farm.app.subscriptions;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    
    List<Subscription> findByStatusAndNextDeliveryDateLessThanEqual(SubscriptionStatus status, LocalDate date);
}
