package de.farm.app.subscriptions;

import java.time.LocalDate;

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
public class Subscription extends BaseEntity{

    @ManyToOne(optional=false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Cadence cadence;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    private LocalDate nextDeliveryDate;
    private String planName;
    private long priceCents;
    private String currency = "FCFA";
    private LocalDate pauseUntil;
    private String paymentProviderRef; // provider subscription id if applicable
}
