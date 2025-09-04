package de.farm.app.reservations;

import java.time.LocalDate;
import java.util.UUID;

public record CreateRequest(
    UUID productId, int quantity, LocalDate pickupDate
) {

}
