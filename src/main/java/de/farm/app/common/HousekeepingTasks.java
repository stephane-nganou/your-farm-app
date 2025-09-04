package de.farm.app.common;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.farm.app.orders.OrderService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HousekeepingTasks {

    private final OrderService orderService;

    @Scheduled(fixedDelay = 300000) // every 5 minutes
    public void expirePendingOrders() {
        orderService.cancelExpiredPendingOrders();
    }
}
