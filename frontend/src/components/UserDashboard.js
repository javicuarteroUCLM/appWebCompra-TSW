/** @format */

import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CardElement, useStripe, useElements } from "@stripe/react-stripe-js";
import { FaShareAlt } from "react-icons/fa"; // Importar ícono de estrella y de compartir
import userService from "../services/userService";
import listService from "../services/listService";
import websocket from "../services/websocket";

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
  const [showEditModal, setShowEditModal] = useState(false);
  const [editProductName, setEditProductName] = useState("");
  const [editProductUdsPedidas, setEditProductUdsPedidas] = useState(0);
  const [editProductUdsCompradas, setEditProductUdsCompradas] = useState(0);
  const [inviteEmail, setInviteEmail] = useState("");
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [showBuyModal, setShowBuyModal] = useState(false);
  const [buyProductUds, setBuyProductUds] = useState(0);
  const [showChangePasswordForm, setShowChangePasswordForm] = useState(false);
  const [newPassword1, setNewPassword1] = useState("");
  const [newPassword2, setNewPassword2] = useState("");
  const [passwordError, setPasswordError] = useState(null);

  const stripe = useStripe();
  const elements = useElements();
  const navigate = useNavigate();
  let ws;

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
    if (selectedProduct) {
      setEditProductName(selectedProduct.nombre);
      setEditProductUdsPedidas(selectedProduct.udsPedidas);
      setEditProductUdsCompradas(selectedProduct.udsCompradas);
    }
  }, [selectedProduct]);

  useEffect(() => {
    if (user && selectedList) {
      ws = listService.conectarWebSocket(selectedList, setProducts);
    }
  }, [user, selectedList, setProducts]);

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

  const handleChangePassword = async () => {
    if (newPassword1 !== newPassword2) {
      setPasswordError("Las contraseñas no coinciden");
      return;
    }

    try {
      await userService.updatePassword(user.email, newPassword1, newPassword2);
      alert("Contraseña actualizada con éxito");
      setShowChangePasswordForm(false);
      setNewPassword1("");
      setNewPassword2("");
      setPasswordError(null);
    } catch (err) {
      console.error("Error actualizando contraseña:", err);
      setPasswordError("No se pudo actualizar la contraseña");
    }
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

  const handleDeleteList = async (listId) => {
    const confirmDelete = window.confirm(
      "¿Estás seguro de que quieres eliminar esta lista?"
    );
    if (!confirmDelete) return;

    try {
      await listService.deleteList(listId);
      setLists((prevLists) => prevLists.filter((list) => list.id !== listId));
      setSelectedList(null);
      alert("Lista eliminada con éxito.");
    } catch (err) {
      console.error("Error eliminando lista:", err);
      setError("No se pudo eliminar la lista.");
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

  const handleDeleteProduct = async (product) => {
    const confirm = window.confirm(
      `¿Estás seguro que quieres eliminar el producto "${product.nombre}" de la lista?`
    );

    if (!confirm) return;

    try {
      await listService.deleteProductFromList(product.id); // Llama al servicio
      setProducts((prevProducts) =>
        prevProducts.filter((p) => p.id !== product.id)
      ); // Actualiza el estado local
      setError(null);
    } catch (err) {
      console.error("Error eliminando producto:", err);
      setError("No se pudo eliminar el producto.");
    }
  };

  const handleEditProduct = (product) => {
    setSelectedProduct(product);
    setShowEditModal(true);
  };

  const handleBuyProduct = (product) => {
    setSelectedProduct(product);
    setBuyProductUds(product.udsCompradas);
    setShowBuyModal(true);
  };

  const handleSaveBuyProduct = async () => {
    try {
      const updatedProduct = await listService.buyProduct(
        selectedProduct.id,
        buyProductUds
      );
  
      setProducts((prevProducts) =>
        prevProducts.map((p) =>
          p.id === updatedProduct.id
            ? {
                ...updatedProduct,
                udsPendientes: updatedProduct.udsPedidas - updatedProduct.udsCompradas, // Cálculo local
              }
            : p
        )
      );
      setShowBuyModal(false);
      setError(null);
    } catch (err) {
      console.error("Error marcando producto como comprado:", err);
      setError("No se pudo marcar el producto como comprado.");
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
      console.log("Actualización recibida:", data);

      // Verificar que la acción sea para la lista seleccionada
      if (data.idLista !== listId) {
        console.warn(`Mensaje para otra lista: ${data.idLista}`);
        return;
      }

      switch (data.action) {
        case "updateProduct":
          console.log("Actualizando producto en la lista...");
          setProducts((prevProducts) =>
            prevProducts.map((p) =>
              p.id === data.producto.id ? data.producto : p
            )
          );
          break;

        case "deleteProduct":
          console.log("Eliminando producto de la lista...");
          setProducts((prevProducts) =>
            prevProducts.filter((p) => p.id !== data.idProducto)
          );
          break;

        case "addProduct":
          console.log("Agregando nuevo producto a la lista...");
          setProducts((prevProducts) => [...prevProducts, data.producto]);
          break;

        default:
          console.warn("Acción no reconocida:", data.action);
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

  const handleSaveEditProduct = async (product) => {
    try {
      product.nombre = editProductName;
      product.udsPedidas = editProductUdsPedidas;
      product.udsCompradas = editProductUdsCompradas;

      await listService.editProductFromList(product);

      setEditProductName("");
      setEditProductUdsPedidas(0);
      setEditProductUdsCompradas(0);
      setShowEditModal(false);

      setError(null);
    } catch (err) {
      console.error("Error editando producto:", err);
      setError("No se pudo editar el producto.");
    }
  };

  const handleCloseEditModal = () => {
    setShowEditModal(false);
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
      {showChangePasswordForm ? (
        <div style={styles.changePasswordForm}>
          <h3>Cambiar Contraseña</h3>
          <input
            type="password"
            value={newPassword1}
            onChange={(e) => setNewPassword1(e.target.value)}
            placeholder="Nueva contraseña"
            style={styles.input}
          />
          <input
            type="password"
            value={newPassword2}
            onChange={(e) => setNewPassword2(e.target.value)}
            placeholder="Confirmar nueva contraseña"
            style={styles.input}
          />
          {passwordError && <p style={styles.error}>{passwordError}</p>}
          <button onClick={handleChangePassword} style={styles.button}>
            Cambiar Contraseña
          </button>
          <button
            onClick={() => setShowChangePasswordForm(false)}
            style={styles.cancelButton}
          >
            Cancelar
          </button>
        </div>
      ) : (
        <button
          onClick={() => setShowChangePasswordForm(true)}
          style={styles.changePasswordButton}
        >
          Cambiar Contraseña
        </button>
      )}
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
            <button
              onClick={() => handleDeleteList(list.id)}
              style={styles.deleteButton}
            >
              Eliminar Lista
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
            <button onClick={handleShareList} style={styles.shareButton}>
              <FaShareAlt /> Compartir Lista
            </button>

            {shareUrl && (
              <p>
                URL generada: <a href={shareUrl}>{shareUrl}</a>
              </p>
            )}
            {error && <p style={{ color: "red" }}>{error}</p>}
          </div>

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
              <li key={product.id} style={styles.productListItem}>
                {product.nombre} - {product.udsPedidas} Unidades pedidas,{" "}
                {product.udsCompradas} Unidades compradas,{" "}
                {product.udsPendientes || product.udsPedidas - product.udsCompradas}{" "}
                Unidades pendientes
                <button
                  style={styles.buyButton}
                  onClick={() => handleBuyProduct(product)}
                >
                  Comprar
                </button>
                <button
                  style={styles.editButton}
                  onClick={() => handleEditProduct(product)}
                >
                  Editar
                </button>
                <button
                  style={styles.deleteButton}
                  onClick={() => handleDeleteProduct(product)}
                >
                  Eliminar
                </button>
              </li>
            ))}
          </ul>

        </div>
      )}
      {showEditModal && (
        <div style={styles.modalOverlay}>
          <div style={styles.modalContent}>
            <h3>Editar Producto</h3>
            <input
              type="text"
              value={editProductName}
              onChange={(e) => setEditProductName(e.target.value)}
              placeholder={selectedProduct?.nombre}
              style={styles.input}
            />
            <input
              type="number"
              value={editProductUdsPedidas}
              onChange={(e) => setEditProductUdsPedidas(Number(e.target.value))}
              placeholder={selectedProduct?.udsPedidas}
              style={{ ...styles.input, width: "80px", marginLeft: "10px" }}
            />
            <input
              type="number"
              value={editProductUdsCompradas}
              disabled
              placeholder={selectedProduct?.udsCompradas}
              style={{
                ...styles.input,
                width: "80px",
                marginLeft: "10px",
                backgroundColor: "#f0f0f0",
                cursor: "not-allowed",
              }}
            />
            <button
              onClick={() => handleSaveEditProduct(selectedProduct)}
              style={styles.button}
            >
              Guardar Cambios
            </button>
            <button onClick={handleCloseEditModal} style={styles.cancelButton}>
              Cancelar
            </button>
          </div>
        </div>
      )}

      {showBuyModal && (
        <div style={styles.modalOverlay}>
          <div style={styles.modalContent}>
            <h3>Marcar como Comprado</h3>
            <p>
              <strong>Producto:</strong> {selectedProduct?.nombre}
            </p>
            <p>
              <strong>Unidades Pedidas:</strong> {selectedProduct?.udsPedidas}
            </p>
            <p>
              <strong>Unidades Compradas:</strong>
            </p>
            <input
              type="number"
              value={buyProductUds}
              onChange={(e) => setBuyProductUds(Number(e.target.value))}
              style={styles.input}
              min={0}
              max={selectedProduct?.udsPedidas}
            />
            <button onClick={handleSaveBuyProduct} style={styles.button}>
              Guardar
            </button>
            <button
              onClick={() => setShowBuyModal(false)}
              style={styles.cancelButton}
            >
              Cancelar
            </button>
          </div>
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
  productListItem: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    padding: "8px 0",
    borderBottom: "1px solid #ddd",
  },
  editButton: {
    backgroundColor: "#4CAF50",
    color: "#fff",
    padding: "5px 10px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
    marginRight: "10px",
  },
  deleteButton: {
    backgroundColor: "#f44336",
    color: "#fff",
    padding: "5px 10px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
  },
  modalOverlay: {
    position: "fixed",
    top: "0",
    left: "0",
    width: "100%",
    height: "100%",
    backgroundColor: "rgba(0, 0, 0, 0.5)",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
  },
  modalContent: {
    backgroundColor: "#fff",
    padding: "20px",
    borderRadius: "5px",
    boxShadow: "0 4px 6px rgba(0, 0, 0, 0.1)",
  },
  cancelButton: {
    backgroundColor: "#f44336",
    color: "#fff",
    padding: "10px 20px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
    margin: "10px",
  },
  buyButton: {
    backgroundColor: "#2196F3",
    color: "#fff",
    padding: "5px 10px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
  },
  changePasswordForm: {
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
  changePasswordButton: {
    backgroundColor: "#2196F3",
    color: "#fff",
    fontSize: "16px",
    padding: "10px 20px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
    marginTop: "20px",
  },
};

export default UserDashboard;
