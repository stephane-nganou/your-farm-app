package de.farm.app.subscriptions;

public record CreateRequest(
    String planName, Cadence cadence, long priceCents, String currency
) {

}
