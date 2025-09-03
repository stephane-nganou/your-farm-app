package de.farm.app.catalog;

import de.farm.app.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;

public class Inventory extends BaseEntity{
    @OneToOne(optional=false)
    private Product product;

    @Column(nullable=false)
    private long quantity; // in smallest unit

    @Column(nullable=false)
    private long reservedQuantity;

    @Version
    private long version;
}
