package de.farm.app.payments;

import java.util.List;

import org.springframework.stereotype.Component;

import de.farm.app.orders.Order;
import de.farm.app.orders.OrderService;
import de.farm.app.payments.dto.PaymentInitResponse;
import lombok.RequiredArgsConstructor;

@Component @RequiredArgsConstructor
public class PaymentFacade {

    private final List<PaymentProvider> providers;
    private final OrderService orderService;

    public PaymentInitResponse initiatePayment(ProviderType type, Order order) {
        return providers.stream()
            .filter(p -> p.type()==type)
            .findFirst()
            .orElseThrow()
            .initiatePayment(order);
    }

    public void onPaymentSucceeded(String providerRef) {
        orderService.markPaid(providerRef);
    }
}
