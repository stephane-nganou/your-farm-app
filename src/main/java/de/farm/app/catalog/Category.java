package de.farm.app.catalog;

import de.farm.app.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter @Setter
public class Category extends BaseEntity {

    @Column(nullable=false, unique=true)
    private String name;

    @Column(nullable=false, unique=true)
    private String slug;
}
