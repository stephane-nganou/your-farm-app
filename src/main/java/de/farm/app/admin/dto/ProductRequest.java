package de.farm.app.admin.dto;

import java.util.UUID;

public record ProductRequest(
    String sku, String name, String description, long priceCents,
    String currency, UUID categoryId, String unit,
    String imageUrl, boolean active, double taxRate) {

}
