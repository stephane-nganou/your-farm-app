package de.farm.app.orders;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import de.farm.app.common.Address;
import de.farm.app.common.BaseEntity;
import de.farm.app.users.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name="orders")
@Getter @Setter
public class Order extends BaseEntity {
    @ManyToOne(optional=false)
    private User user;
    @OneToMany(mappedBy="order", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(nullable=false)
    private long totalCents;

    @Column(nullable=false)
    private String currency = "FCFA";

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private DeliveryMethod deliveryMethod = DeliveryMethod.PICKUP;

    private Instant deliverySlotStart;
    private Instant deliverySlotEnd;
    
    @Embedded private Address deliveryAddress;
    private String notes;

    @OneToOne(mappedBy="order", cascade=CascadeType.ALL)
    private Payment payment;

    private Instant expiresAt; // for pending payment expiration
}
