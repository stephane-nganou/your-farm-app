package de.farm.app.payments.dto;

public record ProviderEvent(
        String providerRef,
        String type,
        String status
        ) {

}
