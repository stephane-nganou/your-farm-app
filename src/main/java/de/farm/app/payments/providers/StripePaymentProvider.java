package de.farm.app.payments.providers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.farm.app.orders.Order;
import de.farm.app.payments.PaymentProvider;
import de.farm.app.payments.ProviderType;
import de.farm.app.payments.dto.PaymentInitResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StripePaymentProvider implements PaymentProvider {

    @Value("${payment.stripe.secretKey:}")
    String secretKey;
    @Value("${payment.stripe.webhookSecret:}")
    String webhookSecret;

    @Override
    public ProviderType type() {

        return ProviderType.STRIPE;
    }

    @Override
    public PaymentInitResponse initiatePayment(Order order) {
        // TODO: Use Stripe SDK to create Checkout Session;
        String sessionId = "cs_test_" + order.getId();
        String redirect = "https://checkout.stripe.com/pay/" + sessionId;
        return new PaymentInitResponse(redirect, sessionId);
    }

    @Override
    public void handleWebhook(String signature, String payload) {
        // TODO: Verify signature with webhookSecret, parse event
        // Example: on 'checkout.session.completed' -> facade.onPaymentSucceeded(sessionId)
        // For now, accept test payload:
        String providerRef = extractRef(payload);
        // In real code, inject PaymentFacade and call it:
        // facade.onPaymentSucceeded(providerRef);
    }

    private String extractRef(String payload) {
        return payload;
    }

}
