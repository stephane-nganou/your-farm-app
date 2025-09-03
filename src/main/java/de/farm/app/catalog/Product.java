package de.farm.app.catalog;

import de.farm.app.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter @Setter
public class Product extends BaseEntity{
    @Column(nullable=false, unique=true)
    private String sku;

    @Column(nullable=false)
    private String name;

    @Column(length=5000)
    private String description;

    @Column(nullable=false)
    private long priceCents;

    @Column(nullable=false)
    private String currency = "FCFA";

    ManyToOne(optional=false) private Category category;
}
