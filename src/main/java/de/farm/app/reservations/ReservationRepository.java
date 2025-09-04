package de.farm.app.reservations;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    
    List<Reservation> findByUser_Id(UUID userId);
}
