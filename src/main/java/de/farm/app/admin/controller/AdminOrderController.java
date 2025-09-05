package de.farm.app.admin.controller;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.farm.app.orders.Order;
import de.farm.app.orders.OrderRepository;
import de.farm.app.orders.OrderStatus;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderRepository orderRepo;

    @GetMapping
    public Page<Order> list(@RequestParam Optional<OrderStatus> status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<Instant> from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<Instant> to,
            Pageable pageable) {

        var page = orderRepo.findAll(pageable);
        var stream = page.stream();
        if (status.isPresent()) {
            stream = stream.filter(order -> order.getStatus() == status.get());
        }
        if (from.isPresent()) {
            stream = stream.filter(order -> !order.getCreatedAt().isBefore(from.get()));
        }
        if (to.isPresent()) {
            stream = stream.filter(order -> !order.getCreatedAt().isAfter(to.get()));
        }
        var filtered = stream.toList();
        return new PageImpl<>(filtered, pageable, page.getTotalElements());
    }

    @PostMapping("/{id}/status")
    public Order setStatus(@PathVariable UUID id, @RequestParam OrderStatus status) {

        var order = orderRepo.findById(id).orElseThrow();
        order.setStatus(status);
        
        return orderRepo.save(order);
    }

}
