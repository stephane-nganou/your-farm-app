package de.farm.app.reservations;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public interface ReservationService {

    public Reservation create(UUID userId, UUID productId, int qty, LocalDate pickupDate);

    public Reservation confirm(UUID id);

    public void cancel(UUID id);

    public List<Reservation> listMine(UUID userId);
}
