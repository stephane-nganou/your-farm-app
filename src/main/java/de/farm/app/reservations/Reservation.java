package de.farm.app.reservations;

import java.time.LocalDate;

import de.farm.app.catalog.Product;
import de.farm.app.common.BaseEntity;
import de.farm.app.users.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter @Setter
public class Reservation extends BaseEntity {

    @ManyToOne(optional=false)
    private User user;

    @ManyToOne(optional=false)
    private Product product;

    @Column(nullable=false)
    private int quantity;

    @Column(nullable=false)
    private LocalDate pickupDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private ReservationStatus status = ReservationStatus.PENDING;
}
