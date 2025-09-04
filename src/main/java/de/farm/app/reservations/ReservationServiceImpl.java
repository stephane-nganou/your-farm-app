package de.farm.app.reservations;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import de.farm.app.catalog.InventoryRepository;
import de.farm.app.catalog.ProductRepository;
import de.farm.app.users.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository repo;
    private final ProductRepository productRepo;
    private final InventoryRepository invRepo;
    private final UserRepository userRepo;

    @Override
    @Transactional
    public Reservation create(UUID userId, UUID productId, int qty, LocalDate pickupDate) {
        var user = userRepo.findById(userId).orElseThrow();
        var product = productRepo.findById(productId).orElseThrow();
        var inv = invRepo.findByProduct_Id(product.getId()).orElseThrow();

        if (inv.getQuantity() - inv.getReservedQuantity() < qty){
            throw new IllegalStateException("Insufficient stock");
        }
        inv.setReservedQuantity(inv.getReservedQuantity() + qty);
        invRepo.save(inv);

        var reservation = new Reservation();
        reservation.setUser(user);
        reservation.setProduct(product);
        reservation.setQuantity(qty);
        reservation.setPickupDate(pickupDate);
        reservation.setStatus(ReservationStatus.PENDING);

        return repo.save(reservation);
    }

    @Override
    @Transactional
    public Reservation confirm(UUID id) {

        var reservation = repo.findById(id).orElseThrow();
        reservation.setStatus(ReservationStatus.CONFIRMED);

        return repo.save(reservation);
    }

    @Override
    @Transactional
    public void cancel(UUID id) {
        var reservation = repo.findById(id).orElseThrow();

        if (reservation.getStatus() == ReservationStatus.CANCELLED){
            return;
        }
        // release reserved
        var inv = invRepo.findByProduct_Id(reservation.getProduct().getId()).orElseThrow();
        inv.setReservedQuantity(Math.max(0, inv.getReservedQuantity() - reservation.getQuantity()));
        invRepo.save(inv);
        reservation.setStatus(ReservationStatus.CANCELLED);

        repo.save(reservation);
    }

    @Override
    public List<Reservation> listMine(UUID userId) {
        
        return repo.findByUser_Id(userId);
    }

}
