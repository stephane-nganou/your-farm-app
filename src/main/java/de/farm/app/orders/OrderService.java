package de.farm.app.orders;

import java.util.UUID;

import org.springframework.stereotype.Service;

import de.farm.app.orders.dto.CreateOrderRequest;
import de.farm.app.payments.ProviderType;

@Service
public interface OrderService {
    public Order createOrder(UUID userId, CreateOrderRequest request);

    public String startPayment(UUID orderId, ProviderType provider);

    public void markPaid(String providerRef);

    public void cancelExpiredPendingOrders();
}
