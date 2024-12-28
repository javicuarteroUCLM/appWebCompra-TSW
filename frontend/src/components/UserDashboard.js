import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import userService from '../services/userService';
import listService from '../services/listService';

const UserDashboard = () => {
  const [user, setUser] = useState(null);
  const [lists, setLists] = useState([]);
  const [newListName, setNewListName] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showPaymentForm, setShowPaymentForm] = useState(false); // si se ha clicado en Hazte Premium, muestra el formulario de pago
  const stripe = useStripe();
  const elements = useElements();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        const response = await userService.getUserDetails();
        setUser(response);
        console.log('User info:', response);

        const userLists = await listService.getUserLists();
        setLists(userLists);
        console.log('User lists:', userLists);
      } catch (error) {
        console.error('Error fetching user info or lists:', error);
        navigate('/login');
      }
    };

    fetchUserInfo();
  }, [navigate]);

  const handleCreateList = async () => {
    const trimmedName = newListName.trim();
    if (!trimmedName) {
      setError('El nombre de la lista no puede estar vacío.');
      return;
    }

    try {
      console.log('Creando lista:', trimmedName);
      const createdList = await listService.createList(trimmedName);
      setLists([...lists, createdList]);
      setNewListName('');
    } catch (error) {
      console.error('Error creating list:', error);
      setError('No se pudo crear la lista.');
    }
  };

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

      const clientSecret = await userService.prepararTransaccion(3);

      const { error, paymentIntent } = await stripe.confirmCardPayment(clientSecret, {
        payment_method: {
          card: cardElement,
          billing_details: {
            name: user.email, //obtiene el email del usuario para que aparezca en las transacciones de stripe
          },
        },
      });

      if (error) {
        console.error(error.message);
        setError(error.message);
        return;
      }

      if (paymentIntent.status === 'succeeded') { //si pago OK
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

      <h2>Mis Listas</h2>
            {error && <p style={{ color: 'red' }}>{error}</p>}
            <div>
              <input
                type="text"
                value={newListName}
                onChange={(e) => setNewListName(e.target.value)}
                placeholder="Nombre de la nueva lista"
              />
              <button onClick={handleCreateList} style={{ marginLeft: '10px' }}>
                Crear Lista
              </button>
            </div>
            <ul style={{ marginTop: '20px' }}>
              {lists.map((list) => (
                <li key={list.id}>{list.nombre}</li>
              ))}
            </ul>
    </div>
  );
};

export default UserDashboard;
