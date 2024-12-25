import React, { useState } from 'react';
import { loadStripe } from '@stripe/stripe-js';
import { Elements, CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import userService from '../services/userService'; 

// Configura tu clave pública de Stripe
const stripePromise = loadStripe('pk_test_YOUR_PUBLIC_KEY_HERE');

const CheckoutForm = () => {
  const [amount, setAmount] = useState('');
  const [message, setMessage] = useState('');
  const stripe = useStripe();
  const elements = useElements();

  const handlePayment = async (e) => {
    e.preventDefault();

    try {
      // Llama al backend para obtener el client_secret
      const clientSecret = await userService.prepareTransaction(amount);

      // Confirma el pago usando Stripe.js
      const result = await stripe.confirmCardPayment(clientSecret, {
        payment_method: {
          card: elements.getElement(CardElement),
        },
      });

      if (result.error) {
        setMessage(`Error: ${result.error.message}`);
      } else if (result.paymentIntent.status === 'succeeded') {
        setMessage('Pago completado con éxito');
      }
    } catch (error) {
      setMessage(`Error: ${error.message}`);
    }
  };

  return (
    <div style={{ textAlign: 'center', marginTop: '50px' }}>
      <h2>Realizar un Pago</h2>
      <form onSubmit={handlePayment}>
        <div>
          <label>Importe (€): </label>
          <input
            type="number"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            required
          />
        </div>
        <div style={{ marginTop: '20px' }}>
          <CardElement />
        </div>
        <button type="submit" disabled={!stripe} style={{ marginTop: '20px', padding: '10px 20px' }}>
          Pagar
        </button>
      </form>
      {message && <p style={{ color: 'red' }}>{message}</p>}
    </div>
  );
};

const PaymentForm = () => (
  <Elements stripe={stripePromise}>
    <CheckoutForm />
  </Elements>
);

export default PaymentForm;
