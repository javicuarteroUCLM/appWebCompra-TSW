/** @format */

import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CardElement, useStripe, useElements } from "@stripe/react-stripe-js";
import { FaStar, FaShareAlt } from "react-icons/fa"; // Importar ícono de estrella y de compartir
import userService from "../services/userService";
import listService from "../services/listService";
import websocket from "../services/websocket";

let ws;

const UserDashboard = () => {
  const [user, setUser] = useState(null);
  const [lists, setLists] = useState([]);
  const [selectedList, setSelectedList] = useState(null);
  const [newListName, setNewListName] = useState("");
  const [newProductName, setNewProductName] = useState("");
  const [newProductQuantity, setNewProductQuantity] = useState(1);
  const [products, setProducts] = useState([]); // Productos de la lista seleccionada
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showPaymentForm, setShowPaymentForm] = useState(false);
  const [shareUrl, setShareUrl] = useState("");
  const [inviteEmail, setInviteEmail] = useState("");

  const stripe = useStripe();
  const elements = useElements();
  const navigate = useNavigate();

  // Conectar WebSocket
  useEffect(() => {
    ws = listService.conectarWebSocket();
  }, []);

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        const response = await userService.getUserDetails();
        setUser(response);

        const userLists = await listService.getUserLists();
        setLists(userLists);
      } catch (error) {
        console.error("Error fetching user info or lists:", error);
        navigate("/login");
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

  // Obtener los productos de la lista seleccionada
  const fetchProducts = async (listId) => {
    try {
      const productList = await listService.getProductsByListId(listId);
      setProducts(productList);
    } catch (err) {
      console.error("Error fetching products:", err);
      setError("Error al cargar los productos de la lista.");
    }
  };

  // Cerrar sesión y borrar Cookies
  const handleLogout = async () => {
    await userService.logout();
    // Eliminar la cookie al cerrar sesión fakeUserID
    document.cookie =
      "fakeUserID=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    // Borrar todas las cookies al cerrar sesión
    document.cookie.split(";").forEach((c) => {
      document.cookie = c
        .replace(/^ +/, "")
        .replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
    });
    // Eliminar el token del localStorage
    localStorage.removeItem("authToken");
    // Limpiar local storage
    localStorage.clear();
    // Limpiar session storage
    sessionStorage.clear();
    navigate("/");
  };

  // Crear una nueva lista
  const handleCreateList = async () => {
    const trimmedName = newListName.trim();
    if (!trimmedName) {
      setError("El nombre de la lista no puede estar vacío.");
      return;
    }

    try {
      const createdList = await listService.createList(trimmedName);
      const userLists = await listService.getUserLists();

      setLists(userLists);
      setNewListName("");
    } catch (error) {
      console.error("Error creando lista:", error);
      if (error.message === "Debes pagar para crear más de 2 listas.") {
        setError(
          "Solo puedes crear 2 listas con tu plan actual. Hazte Premium para crear listas ilimitadas."
        );
      } else {
        setError("No se pudo crear la lista.");
      }
    }
  };

  // Añadir producto a la lista seleccionada
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
    setShareUrl(""); // Limpiar la URL al cambiar de lista
    await fetchProducts(listId); // Carga los productos de la lista seleccionada

    // Suscribirse a actualizaciones en tiempo real para esta lista
    websocket.subscribeToListUpdates(listId, (data) => {
      if (data.action === "updateProduct" && data.idLista === listId) {
        setProducts((prevProducts) => {
          const productoExistente = prevProducts.find(
            (p) => p.id === data.producto.id
          );

          if (productoExistente) {
            // Actualiza el producto existente
            return prevProducts.map((p) =>
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

  // Pasarela de pago para hacer un usuario premium
  const handleGoPremium = () => {
    setShowPaymentForm(true);
    setError(null);
  };

  // Procesar el pago con Stripe
  const handleSubmitPayment = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError(null);

    if (!stripe || !elements) {
      console.error("Stripe no está inicializado.");
      setError("Stripe no está disponible en este momento.");
      return;
    }

    try {
      const cardElement = elements.getElement(CardElement);

      const clientSecret = await userService.prepararTransaccion(3);

      const { error, paymentIntent } = await stripe.confirmCardPayment(
        clientSecret,
        {
          payment_method: {
            card: cardElement,
            billing_details: {
              name: user.email, // Obtiene el email del usuario para que aparezca en las transacciones de stripe
            },
          },
        }
      );

      if (error) {
        console.error(error.message);
        setError(error.message);
        return;
      }

      if (paymentIntent.status === "succeeded") {
        await userService.marcarUsuarioComoPagado(user.email);
        alert("¡Pago procesado con éxito! Ahora eres un usuario premium.");
        window.location.reload();
      }
    } catch (error) {
      console.error("Error al procesar el pago:", error);
      setError("Hubo un problema al procesar el pago. Intenta nuevamente.");
    } finally {
      setLoading(false);
    }
  };

  // Compartir lista
  const handleShareList = async () => {
    if (!selectedList) {
      setError("Por favor, selecciona una lista antes de compartir.");
      return;
    }

    if (!inviteEmail.trim()) {
      setError("Por favor, introduce un email para compartir la lista.");
      return;
    }

    try {
      const url = await listService.shareList(selectedList, inviteEmail); // Pasar emailInvitado
      setShareUrl(url); // Guardar la URL generada
      setError(null);
    } catch (err) {
      console.error("Error compartiendo lista:", err);
      setError("No se pudo compartir la lista.");
    }
  };

  if (!user) {
    return <p>Cargando datos del usuario...</p>;
  }

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>Bienvenido a tu Dashboard</h1>
      <p>
        <strong>Email:</strong> {user.email}
      </p>
      <p>
        <strong>Tipo de usuario:</strong>{" "}
        {user.esPagado ? "Premium" : "Gratuito"}
      </p>
      <button onClick={handleLogout} style={styles.logoutButton}>
        Cerrar Sesión
      </button>
      {!user.esPagado && (
        <>
          {!showPaymentForm ? (
            <button
              onClick={handleGoPremium}
              disabled={loading}
              style={styles.premiumButton}
            >
              Hazte Premium
            </button>
          ) : (
            <form onSubmit={handleSubmitPayment} style={styles.paymentForm}>
              <div style={{ width: "100%", marginBottom: "10px" }}>
                <CardElement
                  options={{ style: { base: styles.cardElement } }}
                />
              </div>
              {error && <p style={styles.error}>{error}</p>}
              <button
                type="submit"
                disabled={loading || !stripe || !elements}
                style={styles.button}
              >
                Confirmar Pago
              </button>
            </form>
          )}
        </>
      )}

      <h2>Mis listas de la compra</h2>
      {error && <p style={styles.error}>{error}</p>}
      <div style={styles.createListContainer}>
        <input
          type="text"
          value={newListName}
          onChange={(e) => setNewListName(e.target.value)}
          placeholder="Nombre de la nueva lista"
          style={styles.input}
        />
        <button onClick={handleCreateList} style={styles.createListButton}>
          Crear Lista
        </button>
      </div>
      <ul style={styles.list}>
        {lists.map((list) => (
          <li key={list.id} style={styles.listItem}>
            {list.nombre}
            <button
              onClick={() => handleSelectList(list.id)}
              style={styles.selectButton}
            >
              Seleccionar
            </button>
          </li>
        ))}
      </ul>
      {selectedList && (
        <div style={styles.selectedListContainer}>
          <h3>Opciones para la Lista Seleccionada</h3>
          <div>
            <h3>Compartir Lista</h3>
            <input
              type="email"
              placeholder="Introduce el email para compartir"
              value={inviteEmail}
              onChange={(e) => setInviteEmail(e.target.value)}
              style={{ marginBottom: "10px", padding: "8px" }}
            />
            <button onClick={handleShareList} style={{ padding: "10px" }}>
              Compartir Lista
            </button>
            {shareUrl && (
              <p>
                URL generada: <a href={shareUrl}>{shareUrl}</a>
              </p>
            )}
            {error && <p style={{ color: "red" }}>{error}</p>}
          </div>
          <button onClick={handleShareList} style={styles.shareButton}>
            <FaShareAlt /> Compartir Lista
          </button>
          {shareUrl && (
            <div style={styles.shareUrlContainer}>
              <p>Comparte esta URL con tus amigos:</p>
              <input
                type="text"
                value={shareUrl}
                readOnly
                style={styles.shareUrlInput}
              />
            </div>
          )}
          <h3>Añadir Producto a la Lista Seleccionada</h3>
          <input
            type="text"
            value={newProductName}
            onChange={(e) => setNewProductName(e.target.value)}
            placeholder="Nombre del producto"
            style={styles.input}
          />
          <input
            type="number"
            value={newProductQuantity}
            onChange={(e) => setNewProductQuantity(Number(e.target.value))}
            placeholder="Cantidad"
            style={{ ...styles.input, width: "80px", marginLeft: "10px" }}
          />
          <button onClick={handleAddProduct} style={styles.button}>
            Añadir Producto
          </button>
          <h3>Productos en la Lista</h3>
          <ul style={styles.productList}>
            {products.map((product) => (
              <li key={product.id}>
                {product.nombre} - {product.udsPedidas} unidades pedidas,{" "}
                {product.udsCompradas} compradas
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
};

const styles = {
  container: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    textAlign: "center",
    padding: "20px",
    minHeight: "100vh",
    backgroundColor: "#f7f9fb",
  },
  header: {
    marginBottom: "30px",
  },
  title: {
    fontSize: "36px",
    color: "#4CAF50",
    margin: "0",
  },
  logoutButton: {
    position: "absolute",
    top: "20px",
    right: "20px",
    backgroundColor: "#ff9800",
    color: "#fff",
    fontSize: "16px",
    padding: "10px 20px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
    display: "flex",
    alignItems: "center",
    gap: "10px",
  },
  premiumButton: {
    backgroundColor: "#ff5722", // Naranja oscuro para premium
    color: "#fff",
    fontSize: "16px",
    padding: "12px 24px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
    margin: "20px 0",
    display: "flex",
    alignItems: "center",
    gap: "10px",
    transition: "background-color 0.3s",
  },
  createListContainer: {
    display: "flex",
    justifyContent: "flex-end",
    width: "100%",
    marginTop: "20px", // Añadimos margen superior para separar de otros elementos
  },
  createListButton: {
    backgroundColor: "#ff9800", // Naranja para crear lista
    color: "#fff",
    fontSize: "16px",
    padding: "12px 24px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
    display: "flex",
    alignItems: "center",
    gap: "10px",
  },
  paymentForm: {
    marginTop: "20px",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    padding: "20px",
    border: "1px solid #ddd",
    borderRadius: "5px",
    backgroundColor: "#fff",
    maxWidth: "400px",
    width: "100%",
    boxShadow: "0 4px 6px rgba(0, 0, 0, 0.1)",
  },
  cardElement: {
    width: "100%",
    padding: "10px",
    fontSize: "16px",
    border: "1px solid #ddd",
    borderRadius: "5px",
    marginBottom: "10px",
    boxSizing: "border-box",
  },
  button: {
    backgroundColor: "#ff9800",
    color: "#fff",
    fontSize: "16px",
    padding: "12px 24px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
    margin: "10px 0",
    transition: "background-color 0.3s",
  },
  error: {
    color: "red",
  },
  listsContainer: {
    width: "100%",
    maxWidth: "600px",
  },
  input: {
    padding: "8px 12px",
    margin: "10px 0",
    width: "100%",
    maxWidth: "300px",
    border: "1px solid #ddd",
    borderRadius: "5px",
  },
  list: {
    listStyleType: "none",
    padding: "0",
  },
  listItem: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    padding: "8px 0",
  },
  selectButton: {
    backgroundColor: "#4CAF50",
    color: "#fff",
    padding: "5px 10px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
  },
  selectedListContainer: {
    marginTop: "20px",
  },
  productList: {
    listStyleType: "none",
    padding: "0",
  },
  shareButton: {
    backgroundColor: "#2196F3",
    color: "#fff",
    padding: "10px 20px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
    display: "flex",
    alignItems: "center",
    gap: "10px",
    margin: "10px 0",
  },
  shareUrlContainer: {
    marginTop: "10px",
    textAlign: "center",
  },
  shareUrlInput: {
    width: "100%",
    padding: "8px",
    marginTop: "5px",
    border: "1px solid #ddd",
    borderRadius: "5px",
  },
};

export default UserDashboard;
