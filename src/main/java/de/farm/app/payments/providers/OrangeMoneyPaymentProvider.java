package de.farm.app.payments.providers;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import de.farm.app.exception.PaymentProviderException;
import de.farm.app.orders.Order;
import de.farm.app.payments.PaymentFacade;
import de.farm.app.payments.PaymentProvider;
import de.farm.app.payments.ProviderType;
import de.farm.app.payments.dto.PaymentInitResponse;

@Component
public class OrangeMoneyPaymentProvider implements PaymentProvider {

    @Value("${payment.orangemoney.oauth.clientId}")
    String clientId;

    @Value("${payment.orangemoney.oauth.clientSecret}")
    String clientSecret;

    @Value("${payment.orangemoney.oauth.tokenUrl}")
    String tokenUrl;

    @Value("${payment.orangemoney.webpay.baseUrl}")
    String baseUrl;

    @Value("${payment.orangemoney.webpay.merchantKey}")
    String merchantKey;

    @Value("${payment.orangemoney.webpay.notifUrl}")
    String notifUrl;

    @Value("${payment.orangemoney.webpay.returnUrl}")
    String returnUrl;

    @Value("${payment.orangemoney.webpay.signatureSecret}")
    String signatureSecret;

    private final PaymentFacade facade;
    private final WebClient web = WebClient.builder().build();
    private final AtomicReference<Token> cachedToken = new AtomicReference<>();

    public OrangeMoneyPaymentProvider(@Lazy PaymentFacade facade){
        this.facade = facade;
    }

    record Token(String value, long expiresAt) {

    }

    @Override
    public ProviderType type() {
        return ProviderType.ORANGEMONEY;
    }

    @Override
    public PaymentInitResponse initiatePayment(Order order) {
        String token = oauthToken();
        String orderId = order.getId().toString();

        String amount = String.format("%.0f", order.getTotalCents() / 100.0);
        // Per OM docs, signature algorithm may vary. Often: HMAC-SHA256 over (orderId + amount + currency + merchantKey)
        String toSign = orderId + amount + order.getCurrency() + merchantKey;
        String signature = hmacSha256Hex(signatureSecret, toSign);

        var body = Map.of(
                "merchant_key", merchantKey,
                "amount", amount,
                "currency", order.getCurrency(),
                "order_id", orderId,
                "return_url", returnUrl,
                "notif_url", notifUrl,
                "lang", "en",
                "reference", "OF-" + orderId,
                "signature", signature
        );

        var resp = web.post().uri(baseUrl + "/webpayment")
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body).retrieve().bodyToMono(Map.class).block();

        Map data = (Map) resp.get("data");
        String payUrl = (String) data.get("payment_url");
        String providerRef = (String) data.get("order_id"); // store OM order id
        return new PaymentInitResponse(payUrl, providerRef);
    }

    @Override
    public void handleWebhook(Map<String, String> headers, String payload) {
        // Validate signature header (scheme varies; verify with Orange docs)
        // Parse payload to check status == SUCCESS
        try {
            var node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(payload);
            String status = node.path("status").asText();
            String orderId = node.path("order_id").asText();
            if ("SUCCESS".equalsIgnoreCase(status)) {
                facade.onPaymentSucceeded(orderId);
            }
        } catch (Exception e) {
            throw new PaymentProviderException("OrangeMoney webhook invalid");
        }
    }

    private String oauthToken() {
        var now = System.currentTimeMillis() / 1000;
        var t = cachedToken.get();

        if (null != t && t.expiresAt > now + 60) {
            return t.value;
        }

        var resp = web.post().uri(tokenUrl)
                .headers(h -> h.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=client_credentials")
                .retrieve().bodyToMono(Map.class).block();

        String token = (String) resp.get("access_token");
        int ttl = ((Number) resp.get("expires_in")).intValue();
        cachedToken.set(new Token(token, now + ttl));
        return token;
    }

    private String hmacSha256Hex(String secret, String msg) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] out = mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : out) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
