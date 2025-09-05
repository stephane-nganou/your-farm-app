package de.farm.app.orders;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import de.farm.app.catalog.InventoryRepository;
import de.farm.app.catalog.ProductRepository;
import de.farm.app.notifications.Notifier;
import de.farm.app.orders.dto.CreateOrderRequest;
import de.farm.app.payments.PaymentFacade;
import de.farm.app.payments.PaymentRepository;
import de.farm.app.payments.ProviderType;
import de.farm.app.users.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ProductRepository productRepo;
    private final InventoryRepository inventoryRepo;
    private final OrderRepository orderRepo;
    private final PaymentRepository paymentRepo;
    private final UserRepository userRepo;
    private final PaymentFacade paymentFacade;
    private final Notifier notifier;

    @Override
    @Transactional
    public Order createOrder(UUID userId, CreateOrderRequest request) {

        var user = userRepo.findById(userId).orElseThrow();
        var order = new Order();
        order.setUser(user);
        order.setDeliveryMethod(request.deliveryMethod());
        order.setNotes(request.notes());
        order.setCurrency("FCFA");

        long total = 0;
        List<OrderItem> items = new ArrayList<>();
        Map<UUID, Integer> requested = new HashMap<>();

        for (var item : request.items()) {
            var product = productRepo.findById(item.productId()).orElseThrow();
            var inv = inventoryRepo.findByProduct_Id(product.getId())
                .orElseThrow(() -> new IllegalStateException("No inventory for product " + product.getName()));

            int qty = item.quantity();
            if (qty <= 0) {
                throw new IllegalArgumentException("Invalid qty");
            }
            // optimistic reserve
            if (inv.getQuantity() - inv.getReservedQuantity() < qty) {
                throw new IllegalStateException("Insufficient stock for " + product.getName());
            }
            inv.setReservedQuantity(inv.getReservedQuantity() + qty);
            inventoryRepo.saveAndFlush(inv);

            var oItem = new OrderItem();
            oItem.setOrder(order);
            oItem.setProductId(product.getId());
            oItem.setProductName(product.getName());
            oItem.setUnitPriceCents(product.getPriceCents());
            oItem.setQuantity(qty);
            oItem.setSubtotalCents(product.getPriceCents() * qty);
            oItem.setUnit(product.getUnit());
            oItem.setImageUrl(product.getImageUrl());
            items.add(oItem);
            total += oItem.getSubtotalCents();

            requested.merge(product.getId(), qty, Integer::sum);
        }

        order.setItems(items);
        order.setTotalCents(total);
        order.setStatus(OrderStatus.PENDING);
        order.setExpiresAt(Instant.now().plusSeconds(15 * 60)); // 15 min hold
        orderRepo.save(order);

        return order;
    }

    @Override
    @Transactional
    public String startPayment(UUID orderId, ProviderType provider) {

        var order = orderRepo.findById(orderId).orElseThrow();

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order not pending");
        }

        var payment = new Payment();
        payment.setOrder(order);
        payment.setAmountCents(order.getTotalCents());
        payment.setCurrency(order.getCurrency());
        payment.setProvider(provider);
        payment.setProviderRef("INIT"); // will be updated by provider
        paymentRepo.save(payment);

        var resp = paymentFacade.initiatePayment(provider, order);
        payment.setProviderRef(resp.providerRef());
        paymentRepo.save(payment);

        return resp.redirectUrl();
    }

    @Override
    @Transactional
    public void markPaid(String providerRef) {

        var payment = paymentRepo.findByProviderRef(providerRef).orElseThrow();
        var order = payment.getOrder();
        if (order.getStatus() == OrderStatus.PAID) {
            return;
        }

        payment.setStatus(PaymentStatus.SUCCEEDED);
        order.setStatus(OrderStatus.PAID);

        // finalize inventory
        for (var item : order.getItems()) {
            var inv = inventoryRepo.findByProduct_Id(item.getProductId()).orElseThrow();
            inv.setReservedQuantity(inv.getReservedQuantity() - item.getQuantity());
            inv.setQuantity(inv.getQuantity() - item.getQuantity());
            inventoryRepo.save(inv);
        }
        orderRepo.save(order);
        notifier.notifyOrderPaid(order);
    }

    @Override
    @Transactional
    public void cancelExpiredPendingOrders() {
        List<Order> ordersList = orderRepo.findByStatusAndExpiresAtBefore(OrderStatus.PENDING, Instant.now());

        for (var order : ordersList) {
            // release inventory
            for (var item : order.getItems()) {
                var inv = inventoryRepo.findByProduct_Id(item.getProductId()).orElse(null);
                if (inv != null) {
                    inv.setReservedQuantity(Math.max(0, inv.getReservedQuantity() - item.getQuantity()));
                    inventoryRepo.save(inv);
                }
            }
            order.setStatus(OrderStatus.CANCELLED);
            orderRepo.save(order);
        }
    }
}
