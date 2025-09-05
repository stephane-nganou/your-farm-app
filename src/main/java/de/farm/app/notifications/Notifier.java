package de.farm.app.notifications;

import de.farm.app.orders.Order;

public interface Notifier {

   public void notifyOrderPaid(Order order);
}
