package de.farm.app.orders;

import java.util.UUID;

import de.farm.app.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;

public class OrderItem extends BaseEntity {

    @ManyToOne(optional=false)
    private Order order;

    @Column(nullable=false)
    private UUID productId;

    @Column(nullable=false)
    private String productName;

    @Column(nullable=false)
    private long unitPriceCents;

    @Column(nullable=false)
    private int quantity;

    @Column(nullable=false)
    private long subtotalCents;
    
    private String unit;
    private String imageUrl;
}
