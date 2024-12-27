import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import userService from '../services/userService';

const UserDashboard = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showPaymentForm, setShowPaymentForm] = useState(false); // Nuevo estado
  const stripe = useStripe();
  const elements = useElements();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        const response = await userService.getUserDetails();
        setUser(response);
        console.log('User info:', response);
      } catch (error) {
        console.error('Error fetching user info:', error);
        navigate('/login');
      }
    };

    fetchUserInfo();
  }, [navigate]);

  const handleGoPremium = () => {
    setShowPaymentForm(true); 
    setError(null);
  };

  const handleSubmitPayment = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError(null);

    if (!stripe || !elements) {
      console.error('Stripe no está inicializado.');
      setError('Stripe no está disponible en este momento.');
      return;
    }

    try {
      const cardElement = elements.getElement(CardElement);

      // Obtener el clientSecret desde el backend
      const clientSecret = await userService.prepararTransaccion(3);

      const { error, paymentIntent } = await stripe.confirmCardPayment(clientSecret, {
        payment_method: {
          card: cardElement,
          billing_details: {
            name: user.email, // Puedes reemplazar con el nombre del usuario si lo tienes
          },
        },
      });

      if (error) {
        console.error(error.message);
        setError(error.message);
        return;
      }

      if (paymentIntent.status === 'succeeded') {
        // Pago exitoso: llama al backend para marcar al usuario como premium
        await userService.marcarUsuarioComoPagado(user.email);
        alert('¡Pago procesado con éxito! Ahora eres un usuario premium.');
        window.location.reload();
      }
    } catch (error) {
      console.error('Error al procesar el pago:', error);
      setError('Hubo un problema al procesar el pago. Intenta nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  if (!user) {
    return <p>Cargando datos del usuario...</p>;
  }

  return (
    <div>
      <h1>Bienvenido a tu Dashboard</h1>
      <p><strong>Email:</strong> {user.email}</p>
      <p><strong>Tipo de usuario:</strong> {user.esPagado ? 'Premium' : 'Gratuito'}</p>
      {!user.esPagado && (
        <>
          {!showPaymentForm ? (
            <button
              onClick={handleGoPremium}
              disabled={loading}
              style={{ marginTop: '20px', padding: '10px 20px' }}
            >
              Hazte Premium
            </button>
          ) : (
            <form onSubmit={handleSubmitPayment} style={{ marginTop: '20px' }}>
              <CardElement />
              {error && <p style={{ color: 'red' }}>{error}</p>}
              <button type="submit" disabled={loading || !stripe || !elements}>
                Confirmar Pago
              </button>
            </form>
          )}
        </>
      )}
    </div>
  );
};

export default UserDashboard;
