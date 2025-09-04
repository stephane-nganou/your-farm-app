package de.farm.app.content;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TestimonialRepository extends JpaRepository<Testimonial, UUID>{

}
