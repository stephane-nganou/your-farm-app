package de.farm.app.payments;

import java.util.Map;

import de.farm.app.orders.Order;
import de.farm.app.payments.dto.PaymentInitResponse;

public interface PaymentProvider {

    ProviderType type();

    PaymentInitResponse initiatePayment(Order order);

    void handleWebhook(Map<String, String> headers, String payload);
}
