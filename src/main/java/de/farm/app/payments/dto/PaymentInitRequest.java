package de.farm.app.payments.dto;

public record PaymentInitRequest(
        String orderId,
        long amountCents,
        String currency,
        String successUrl,
        String cancelUrl
        ) {

}
