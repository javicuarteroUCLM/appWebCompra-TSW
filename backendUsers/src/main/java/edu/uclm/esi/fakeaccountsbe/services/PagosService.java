package edu.uclm.esi.fakeaccountsbe.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PagosService {
    // Pregunta de examen para que sirve la clave publica y privada de stripe

    private final Manager manager;

    @Autowired
    public PagosService(Manager manager) throws org.json.JSONException {
        this.manager = manager;
        Stripe.apiKey = manager.getConfiguration().getJSONObject("stripe").getString("clavePrivadaStripe");
    }

    public String prepararTransaccion(long importe) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(importe)
                    .setCurrency("eur")
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            return paymentIntent.getClientSecret(); // Debe devolver el client secret o URL correcta
        } catch (StripeException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al preparar la transacción en Stripe", e);
        }
    }

    public String getStripeKey() throws org.json.JSONException {
        return this.manager.getConfiguration().getJSONObject("stripe").getString("clavePublicaStripe");
    }

}