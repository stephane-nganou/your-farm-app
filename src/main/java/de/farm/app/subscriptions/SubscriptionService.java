package de.farm.app.subscriptions;

import java.util.UUID;


public interface SubscriptionService {

    public Subscription create(UUID userId, 
        String planName, Cadence cadence, long priceCents, String currency);

    public void generateUpcomingOrders();
}
