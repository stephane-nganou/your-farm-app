package de.farm.app.payments;

import de.farm.app.orders.Order;
import de.farm.app.payments.dto.PaymentInitResponse;

public interface PaymentProvider {

    ProviderType type();

    PaymentInitResponse initiatePayment(Order order);

    void handleWebhook(String signature, String payload);
}
