package de.farm.app.notifications;

import org.springframework.stereotype.Service;

import de.farm.app.orders.Order;

@Service
public interface Notifier {

   public void notifyOrderPaid(Order order);
}
