package de.farm.app.reservations;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<Reservation> create(@AuthenticationPrincipal String userId,
            @RequestBody CreateRequest req) {

        var reservation = reservationService.create(UUID.fromString(userId),
                req.productId(), req.quantity(), req.pickupDate());
        return ResponseEntity.ok(reservation);
    }

    @GetMapping
    public List<Reservation> mine(@AuthenticationPrincipal String userId) {
        return reservationService.listMine(UUID.fromString(userId));
    }
}
