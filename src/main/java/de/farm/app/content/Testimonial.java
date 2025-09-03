package de.farm.app.content;

import de.farm.app.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter @Setter
public class Testimonial extends BaseEntity{

    private String author;
    private int rating;

    @Column(length=2000)
    private String text;
    
    private boolean published=true;
}
