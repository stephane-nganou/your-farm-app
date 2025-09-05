package de.farm.app.subscriptions;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.farm.app.orders.DeliveryMethod;
import de.farm.app.orders.OrderService;
import de.farm.app.orders.dto.CreateOrderRequest;
import de.farm.app.orders.dto.Item;
import de.farm.app.users.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subRepo;
    private final UserRepository userRepo;
    private final OrderService orderService;

    @Override
    @Transactional
    public Subscription create(UUID userId, String planName,
            Cadence cadence, long priceCents, String currency) {

        var user = userRepo.findById(userId).orElseThrow();
        var subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlanName(planName);
        subscription.setCadence(cadence);
        subscription.setPriceCents(priceCents);
        subscription.setCurrency(currency);
        subscription.setNextDeliveryDate(LocalDate.now().plusWeeks(1));

        return subRepo.save(subscription);
    }

    @Override
    @Scheduled(cron = "0 0 2 * * *") // daily 2am
    @Transactional
    public void generateUpcomingOrders() {
        var today = LocalDate.now();
        List<Subscription> dueList = subRepo.findByStatusAndNextDeliveryDateLessThanEqual(SubscriptionStatus.ACTIVE, today);
        for (var due : dueList) {
            //TODO For MVP: create a fixed "box" order; in real app, resolve box composition by plan.
            var boxItem = new Item(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"), 1); // placeholder; map to a "Farm Box" product
            var req = new CreateOrderRequest(java.util.List.of(boxItem), DeliveryMethod.PICKUP, "Subscription box");
            // TODO
            // Payment initiation could be automatic (if provider supports off-session)
            // Advance next date
            var order = orderService.createOrder(due.getUser().getId(), req);
            // Payment initiation could be automatic (if provider supports off-session)
            // Advance next date
            switch (due.getCadence()) {
                case WEEKLY ->
                    due.setNextDeliveryDate(due.getNextDeliveryDate().plusWeeks(1));
                case BIWEEKLY ->
                    due.setNextDeliveryDate(due.getNextDeliveryDate().plusWeeks(2));
                case MONTHLY ->
                    due.setNextDeliveryDate(due.getNextDeliveryDate().plusMonths(1));
            }
            subRepo.save(due);
        }
    }

}
