package de.farm.app.payments;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import de.farm.app.orders.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID>{

    Optional<Payment> findByProviderRef(String providerRef);
}
