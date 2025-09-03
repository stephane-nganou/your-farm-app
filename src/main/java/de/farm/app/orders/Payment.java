package de.farm.app.orders;

import de.farm.app.common.BaseEntity;
import de.farm.app.payments.ProviderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter @Setter
public class Payment extends BaseEntity {

    @OneToOne(optional=false)
    @JoinColumn(name="order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private ProviderType provider;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable=false)
    private long amountCents;

    @Column(nullable=false)
    private String currency = "FCFA";

    @Column(nullable=false) 
    private String providerRef; // e.g., Stripe session id
}
