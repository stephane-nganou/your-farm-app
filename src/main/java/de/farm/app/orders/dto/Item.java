package de.farm.app.orders.dto;

import java.util.UUID;

public record Item(
    UUID productId,
    int quantity
) {

}
