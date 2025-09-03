package de.farm.app.content;

import java.time.Instant;

import de.farm.app.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter @Setter
public class Event extends BaseEntity{

    @Column(nullable=false) private String title;
    private String description;
    private Instant startAt;
    private Instant endAt;
    private String location;
    private Integer ticketsLimit;
}
