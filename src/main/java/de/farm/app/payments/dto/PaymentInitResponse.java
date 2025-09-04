package de.farm.app.payments.dto;

public record PaymentInitResponse(
        String redirectUrl,
        String providerRef
        ) {

}
