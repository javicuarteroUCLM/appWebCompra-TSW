package edu.uclm.esi.fakeaccountsbe.services;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

@Service
public class PagosService {
    // Pregunta de examen para que sirve la clave publica y privada de stripe

    static {
        Stripe.apiKey = "sk_test_51Q7a2R06d48jei1Jd8T9WwlDzDXG4WeSF9yoJyNcgdc3GRRDq3AS55kQIVZXieZKH3Yy47h0vhntwp8v6WuReZjx00hZLUcjV9";
    }

    public String prepararTransaccion(long importe) {
        PaymentIntentCreateParams params = new PaymentIntentCreateParams.Builder()
                .setCurrency("eur")
                .setAmount(importe)
                .build();

        PaymentIntent intent;

        try {
            intent = PaymentIntent.create(params);
            JSONObject jso = new JSONObject(intent.toJson());
            String clientSecret = jso.getString("client_secret");
            return clientSecret;
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

    }
}
