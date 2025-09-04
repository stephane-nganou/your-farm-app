package de.farm.app.content;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ContentController {

    private final EventRepository events;
    private final TestimonialRepository testi;

    public ContentController(EventRepository e, TestimonialRepository t) {
        this.events = e;
        this.testi = t;
    }

    @GetMapping("/events")
    public List<Event> events() {
        return events.findAll();
    }

    @GetMapping("/testimonials")
    public List<Testimonial> testimonials() {
        return testi.findAll().stream().filter(Testimonial::isPublished).toList();
    }
}
