package de.farm.app.payments.providers;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.ApplicationContext;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersCreateRequest;
import com.paypal.orders.PurchaseUnitRequest;

import de.farm.app.exception.PaymentProviderException;
import de.farm.app.orders.Order;
import de.farm.app.payments.PaymentFacade;
import de.farm.app.payments.PaymentProvider;
import de.farm.app.payments.ProviderType;
import de.farm.app.payments.dto.PaymentInitResponse;

@Component
public class PaypalPaymentProvider implements PaymentProvider {

    @Value("${payment.paypal.clientId}")
    String clientId;

    @Value("${payment.paypal.clientSecret}")
    String clientSecret;

    @Value("${payment.paypal.environment:sandbox}")
    String env;

    @Value("${payment.paypal.webhookId}")
    String webhookId;

    @Value("${payment.paypal.base-url}")
    String paypalBaseUrl;

    @Value("${frontend.base-url}")
    String frontendBaseUrl;

    private final PaymentFacade facade;

    private final RestTemplate restTemplate;

    @Autowired
    public PaypalPaymentProvider(RestTemplate restTemplate, @Lazy PaymentFacade facade){
        this.restTemplate = restTemplate;
        this.facade = facade;
    }

    @Override
    public ProviderType type() {
        return ProviderType.PAYPAL;
    }

    @Override
    public PaymentInitResponse initiatePayment(Order order) {
        OrdersCreateRequest request = new OrdersCreateRequest();

        request.requestBody(new OrderRequest()
                .checkoutPaymentIntent("CAPTURE")
                .purchaseUnits(List.of(new PurchaseUnitRequest()
                        .amountWithBreakdown(new AmountWithBreakdown()
                                .currencyCode(order.getCurrency())
                                .value(String.format("%.2f", order.getTotalCents() / 100.0)))))
                .applicationContext(
                        new ApplicationContext()
                                .brandName("Le Bon Pork")
                                .landingPage(frontendBaseUrl)
                                .returnUrl(frontendBaseUrl + "/checkout/result?provider=paypal")
                                .cancelUrl(frontendBaseUrl + "/checkout/cancel")
                )
        );
        try {
            HttpResponse<com.paypal.orders.Order> response = client().execute(request);
            String orderId = response.result().id();
            String approveLink = response.result().links().stream()
                    .filter(l -> "approve".equalsIgnoreCase(l.rel())).findFirst().orElseThrow().href();
            return new PaymentInitResponse(approveLink, orderId);
        } catch (Exception e) {
            // TODO: Bessere handling
            throw new RuntimeException("PayPal init failed", e);
        }
    }

    @Override
    public void handleWebhook(Map<String, String> headers, String payload) {
        // Verify webhook signature using PayPal verify API
        // Docs: POST /v1/notifications/verify-webhook-signature
        // Required headers: transmission_id, transmission_time, cert_url, auth_algo, transmission_sig
        // We assume WebhookController passes all headers; here we demonstrate WebClient call:

        // If verified and event type PAYMENT.CAPTURE.COMPLETED -> extract related order id and mark success
        // For brevity, assume verification succeeded and the payload contains order.id

        // Step 1: Verify the signature before processing
        String defaultHeaderValue = "";
        boolean isVerified = verifyWebhookSignature(
            headers.getOrDefault("PayPal-Transmission-Id", defaultHeaderValue),
            headers.getOrDefault("PayPal-Transmission-Time", defaultHeaderValue),
            headers.getOrDefault("PayPal-Transmission-Sig", defaultHeaderValue),
            headers.getOrDefault("PayPal-Cert-Url", defaultHeaderValue),
            headers.getOrDefault("PayPal-Auth-Algo", defaultHeaderValue), payload
        );
        if (!isVerified) {
            throw new PaymentProviderException("Webhook verification failed");
        }
        
        // Step 2: If verified, process the event (e.g., parse rawBody as JSON and handle
        String providerRef = extractPaypalOrderId(payload); // parse JSON
        if (providerRef != null) {
            facade.onPaymentSucceeded(providerRef);
        }
    }

    private boolean verifyWebhookSignature(String transmissionId, String transmissionTime, String transmissionSig,
            String certUrl, String authAlgo, String rawBody) {

        // Get access token
        String accessToken = getAccessToken();
        if (StringUtils.isBlank(accessToken)) {
            return false;
        }

        // Build verification request body as string to preserve original rawBody order/structure
        String verifyBody = "{"
                + "\"auth_algo\": \"" + authAlgo + "\","
                + "\"cert_url\": \"" + certUrl + "\","
                + "\"transmission_id\": \"" + transmissionId + "\","
                + "\"transmission_sig\": \"" + transmissionSig + "\","
                + "\"transmission_time\": \"" + transmissionTime + "\","
                + "\"webhook_id\": \"" + webhookId + "\","
                + "\"webhook_event\": " + rawBody
                + "}";

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(verifyBody, headers);

        // Call PayPal verify API
        String verifyUrl = paypalBaseUrl + "/v1/notifications/verify-webhook-signature";
        ResponseEntity<String> response = restTemplate.postForEntity(verifyUrl, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            // Parse response (simple string check; use JSON parser for production)
            return response.getBody().contains("\"verification_status\":\"SUCCESS\"");
        }
        return false;
    }

    private String getAccessToken() {
        String authUrl = paypalBaseUrl + "/v1/oauth2/token";
        String credentials = clientId+ ":" + clientSecret;
        String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + base64Credentials);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>("grant_type=client_credentials", headers);

        ResponseEntity<String> response = restTemplate.postForEntity(authUrl, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            // Parse access_token from response (simple string extract; use JSON parser in production)
            String body = response.getBody();
            int start = body.indexOf("\"access_token\":\"") + 16;
            int end = body.indexOf("\"", start);
            return body.substring(start, end);
        }
        return null;
    }

    private String extractPaypalOrderId(String payload) {
        // Parse JSON and return resource.supplementary_data.related_ids.order_id OR resource.id for order events
        try {
            var node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(payload);
            if (node.has("resource")) {
                var res = node.get("resource");
                if (res.has("id")) {
                    return res.get("id").asText();
                }
                if (res.has("supplementary_data") && res.get("supplementary_data").has("related_ids")
                        && res.get("supplementary_data").get("related_ids").has("order_id")) {
                    return res.get("supplementary_data").get("related_ids").get("order_id").asText();
                }
            }
        } catch (Exception ignored) {
            // TODO: Bessere handling
        }
        return null;
    }

    private PayPalHttpClient client() {
        PayPalEnvironment environment = "live".equalsIgnoreCase(env)
                ? new PayPalEnvironment.Live(clientId, clientSecret)
                : new PayPalEnvironment.Sandbox(clientId, clientSecret);
        return new PayPalHttpClient(environment);
    }
}
