package edu.uclm.esi.fakeaccountsbe.services;

import org.springframework.beans.factory.annotation.Autowired;
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

    private final Manager manager;

    @Autowired
    public PagosService(Manager manager) {
        this.manager = manager;
        Stripe.apiKey = manager.getConfiguration().getJSONObject("stripe").getString("clavePrivadaStripe");
    }

    public String prepararTransaccion(long importe) {
        PaymentIntentCreateParams params = new PaymentIntentCreateParams.Builder()
                .setCurrency("eur")
                .setAmount(importe)
                .build();

        PaymentIntent intent;

        try {
            intent = PaymentIntent.create(params);
            return intent.getClientSecret();
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating payment intent", e);
        }
    }
}