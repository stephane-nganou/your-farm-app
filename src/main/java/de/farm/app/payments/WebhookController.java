package de.farm.app.payments;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments/webhooks")
public class WebhookController {

    //private final PaymentFacade facade;
    private final List<PaymentProvider> providers;

    public WebhookController(List<PaymentProvider> providers) {
        //this.facade = facade;
        this.providers = providers;
    }

    @PostMapping("/{provider}")
    public ResponseEntity<Void> webhook(@PathVariable String provider, 
        @RequestHeader(value = "X-Signature", required = false) String sig,
        @RequestBody String payload) {
            
        var type = ProviderType.valueOf(provider.toUpperCase());
        providers.stream().filter(prov -> prov.type() == type)
            .findFirst().orElseThrow().handleWebhook(sig, payload);

        return ResponseEntity.ok().build();
    }
}
