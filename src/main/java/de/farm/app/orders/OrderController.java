package de.farm.app.orders;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.farm.app.orders.dto.CreateOrderRequest;
import de.farm.app.orders.dto.Item;
import de.farm.app.payments.ProviderType;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> create(
            @AuthenticationPrincipal String userId, @Valid @RequestBody CreateOrderRequest request) {

        var order = orderService.createOrder(UUID.fromString(userId),
                new CreateOrderRequest(
                        request.items().stream().map(i -> new Item(i.productId(), i.quantity())).toList(),
                        request.deliveryMethod(),
                        request.notes()
                ));

        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<Map<String, String>> pay(@PathVariable UUID id,
            @RequestParam(defaultValue = "STRIPE") ProviderType provider) {

        var redirect = orderService.startPayment(id, provider);

        return ResponseEntity.ok(Map.of("redirectUrl", redirect));
    }

}
