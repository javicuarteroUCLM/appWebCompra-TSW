import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import userService from '../services/userService';
import listService from '../services/listService';
import websocket from '../services/websocket';

const UserDashboard = () => {
  const [user, setUser] = useState(null);
  const [lists, setLists] = useState([]);
  const [selectedList, setSelectedList] = useState(null);
  const [newListName, setNewListName] = useState('');
  const [newProductName, setNewProductName] = useState('');
  const [newProductQuantity, setNewProductQuantity] = useState(1);
  const [products, setProducts] = useState([]); // Productos de la lista seleccionada
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showPaymentForm, setShowPaymentForm] = useState(false);
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

  useEffect(() => {
    return () => {
      if (selectedList) {
        websocket.unsubscribeFromListUpdates(selectedList);
      }
    };
  }, [selectedList]);

  const fetchProducts = async (listId) => {
    try {
      const productList = await listService.getProductsByListId(listId);
      setProducts(productList);
    } catch (err) {
      console.error('Error fetching products:', err);
      setError('Error al cargar los productos de la lista.');
    }
  };

  const handleLogout = async () => {
    await userService.logout();
    navigate('/');
  };

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
      console.error('Error creando lista:', error);
      if (error.message === 'Debes pagar para crear más de 2 listas.') {
        setError(
          'Solo puedes crear 2 listas con tu plan actual. Hazte Premium para crear listas ilimitadas.'
        );
      } else {
        setError('No se pudo crear la lista.');
      }
    }
  };

  const handleAddProduct = async () => {
    if (!selectedList) {
      setError("Por favor, selecciona una lista.");
      return;
    }
  
    if (!newProductName.trim()) {
      setError("El nombre del producto no puede estar vacío.");
      return;
    }
  
    if (newProductQuantity <= 0) {
      setError("La cantidad debe ser mayor que 0.");
      return;
    }
  
    try {
      const product = {
        nombre: newProductName,
        udsPedidas: newProductQuantity,
        udsCompradas: 0,
      };
  
      await listService.addProductToList(selectedList, product);
      setNewProductName("");
      setNewProductQuantity(1);
      setError(null);
    } catch (err) {
      console.error("Error añadiendo producto:", err);
      setError("No se pudo añadir el producto.");
    }
  };
  

  const handleSelectList = async (listId) => {
    if (selectedList) {
        websocket.unsubscribeFromListUpdates(selectedList);
    }

    setSelectedList(listId);
    setProducts([]); 
    await fetchProducts(listId); // Carga los productos de la lista seleccionada

    // Suscribirse a actualizaciones en tiempo real para esta lista
    websocket.subscribeToListUpdates(listId, (data) => {
      if (data.action === 'updateProduct' && data.idLista === listId) {
          setProducts((prevProducts) => {
              const productoExistente = prevProducts.find(p => p.id === data.producto.id);
              if (productoExistente) {
                  // Actualiza el producto existente
                  return prevProducts.map(p => 
                      p.id === data.producto.id ? data.producto : p
                  );
              } else {
                  // Agrega el nuevo producto
                  return [...prevProducts, data.producto];
              }
          });
      }
  });  
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
            name: user.email, // Obtiene el email del usuario para que aparezca en las transacciones de stripe
          },
        },
      });

      if (error) {
        console.error(error.message);
        setError(error.message);
        return;
      }

      if (paymentIntent.status === 'succeeded') {
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
      <button onClick={handleLogout} style={{ marginBottom: '20px', padding: '10px 20px' }}>
        Cerrar Sesión
      </button>
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

      <h2>Mis listas de la compra</h2>
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
          <li key={list.id}>
            {list.nombre}
            <button
              onClick={() => handleSelectList(list.id)}
              style={{ marginLeft: '10px', padding: '5px' }}
            >
              Seleccionar
            </button>
          </li>
        ))}
      </ul>
      {selectedList && (
        <div style={{ marginTop: '20px' }}>
          <h3>Añadir Producto a la Lista Seleccionada</h3>
          <input
            type="text"
            value={newProductName}
            onChange={(e) => setNewProductName(e.target.value)}
            placeholder="Nombre del producto"
          />
          <input
            type="number"
            value={newProductQuantity}
            onChange={(e) => setNewProductQuantity(Number(e.target.value))}
            placeholder="Cantidad"
            style={{ marginLeft: '10px', width: '80px' }}
          />
          <button onClick={handleAddProduct} style={{ marginLeft: '10px' }}>
            Añadir Producto
          </button>
          <h3>Productos en la Lista</h3>
          <ul>
            {products.map((product) => (
              <li key={product.id}>
                {product.nombre} - {product.udsPedidas} unidades pedidas, {product.udsCompradas} compradas
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
};

export default UserDashboard;
