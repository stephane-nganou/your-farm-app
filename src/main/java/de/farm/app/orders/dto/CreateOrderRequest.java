package de.farm.app.orders.dto;

import java.util.List;

import de.farm.app.orders.DeliveryMethod;

public record CreateOrderRequest(
    List<Item> items,
    DeliveryMethod deliveryMethod,
    String notes
) {

}
