package de.farm.app.orders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByStatusAndExpiresAtBefore(OrderStatus status, Instant time);

    List<Order> findByUser_Id(UUID userId);

}
