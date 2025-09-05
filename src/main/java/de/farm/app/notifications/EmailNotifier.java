package de.farm.app.notifications;

import org.springframework.stereotype.Service;

import de.farm.app.orders.Order;

@Service
public class EmailNotifier implements Notifier {

    @Override
    public void notifyOrderPaid(Order order) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notifyOrderPaid'");
    }

}
