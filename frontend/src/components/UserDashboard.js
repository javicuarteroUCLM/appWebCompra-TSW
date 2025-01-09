/** @format */

import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CardElement, useStripe, useElements } from "@stripe/react-stripe-js";
import {
  FaShareAlt,
  FaTrashAlt,
  FaExchangeAlt,
  FaUserAlt,
} from "react-icons/fa";
import { MdWorkspacePremium } from "react-icons/md";
import { CiLogout } from "react-icons/ci";
import userService from "../services/userService";
import listService from "../services/listService";
import { router } from "websocket";

const UserDashboard = () => {
  const [user, setUser] = useState(null);
  const [lists, setLists] = useState([]);
  const [selectedList, setSelectedList] = useState(null);
  const [miembrosList, setMiembrosList] = useState(null);
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
  const [inviteSent, setInviteSent] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [showBuyModal, setShowBuyModal] = useState(false);
  const [showMiembrosModal, setShowMiembrosModal] = useState(false);
  const [buyProductUds, setBuyProductUds] = useState(0);
  const [showChangePasswordForm, setShowChangePasswordForm] = useState(false);
  const [newPassword1, setNewPassword1] = useState("");
  const [newPassword2, setNewPassword2] = useState("");
  const [passwordError, setPasswordError] = useState(null);
  const [listError, setListError] = useState(null);
  const [productError, setProductError] = useState(null);
  const [memberError, setMemberError] = useState(null);
  const [shareListError, setShareListError] = useState(null);
  const [pendingInvitations, setPendingInvitations] = useState([]);
  const [showShareModal, setShowShareModal] = useState(false);
  const [showListModal, setShowListModal] = useState(false);
  const [addProductMessage, setAddProductMessage] = useState("");

  const stripe = useStripe();
  const elements = useElements();
  const navigate = useNavigate();
  let ws;

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        sessionStorage.setItem("authToken", localStorage.getItem("authToken"));

        const userDetails = await userService.getUserDetails();
        setUser(userDetails);

        const userLists = await listService.getUserLists();
        setLists(userLists);

        setSelectedList(null);
        setProducts([]);
      } catch (error) {
        console.error("Error fetching user info or lists:", error);
        navigate("/login"); // Redirige a la página de inicio de sesión si hay un error
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

  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        const invitations = await listService.getInvitations();
        const pending = invitations.filter((inv) => inv.estado === "PENDIENTE");
        setPendingInvitations(pending); // Actualiza el estado con las invitaciones pendientes
        if (pending.length > 0) {
          setError(`Tienes ${pending.length} invitación(es) pendiente(s).`);
        } else {
          setError(null); // Limpia el error si no hay invitaciones
        }
      } catch (err) {
        console.log("Error al verificar invitaciones pendientes:", err);
      }
    }, 3000);

    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (showListModal) {
      document.body.style.overflow = "hidden"; // Deshabilita scroll en el fondo
    } else {
      document.body.style.overflow = "auto"; // Habilita scroll cuando el modal se cierra
    }
  }, [showListModal]);

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
    setUser(null);
    setLists([]);
    setProducts([]);
    setSelectedList(null);
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
      setListError("El nombre de la lista no puede estar vacío.");
      return;
    }

    try {
      const createdList = await listService.createList(trimmedName);
      const userLists = await listService.getUserLists();

      setLists(userLists);
      setNewListName("");
      setListError(null); // Limpiar el error si la operación fue exitosa
    } catch (error) {
      console.error("Error creando lista:", error);
      if (error.message === "Debes pagar para crear más de 2 listas.") {
        setListError(
          "Solo puedes crear 2 listas con tu plan actual. Hazte Premium para crear listas ilimitadas."
        );
      } else {
        setListError("No se pudo crear la lista.");
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
      setListError(null); // Limpiar el error si la operación fue exitosa
    } catch (error) {
      console.error("Error eliminando lista:", error);
      if (error.message === "Solo el propietario puede eliminar la lista.") {
        setListError(
          "No tienes permiso para borrar esta lista. Solo el propietario de la lista puede hacerlo."
        );
      } else {
        setListError("No se pudo eliminar la lista.");
      }
    }
  };

  // Añadir producto a la lista seleccionada
  const handleAddProduct = async () => {
    if (!selectedList) {
      setProductError("Por favor, selecciona una lista.");
      return;
    }

    if (!newProductName.trim()) {
      setProductError("El nombre del producto no puede estar vacío.");
      return;
    }

    if (newProductQuantity <= 0) {
      setProductError("La cantidad debe ser mayor que 0.");
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
      setProductError(null); // Limpiar error al éxito
      setAddProductMessage("Producto añadido correctamente.");
      setTimeout(() => setAddProductMessage(""), 3000);
    } catch (err) {
      console.error("Error añadiendo producto:", err);
      setProductError("No se pudo añadir el producto.");
    }
  };

  const handleDeleteProduct = async (product) => {
    const confirm = window.confirm(
      `¿Estás seguro que quieres eliminar el producto "${product.nombre}" de la lista?`
    );

    if (!confirm) return;

    try {
      await listService.deleteProductFromList(product.id);
      setProducts((prevProducts) =>
        prevProducts.filter((p) => p.id !== product.id)
      );
      setProductError(null); // Limpiar error al éxito
    } catch (err) {
      console.error("Error eliminando producto:", err);
      setProductError("No se pudo eliminar el producto.");
    }
  };

  const handleEditProduct = (product) => {
    setSelectedProduct(product);
    setShowEditModal(true);
  };

  const handleBuyProduct = (product) => {
    setSelectedProduct(product);
    setBuyProductUds(1);
    setShowBuyModal(true);
  };

  const handleMiembros = async (listaId) => {
    setSelectedList(listaId);

    try {
      const miembros = await listService.getMiembros(listaId);
      setMiembrosList(miembros);
      console.log("Miembros de la lista:", miembros);
      setMemberError(null); // Limpiar error en caso de éxito
    } catch (err) {
      console.error("Error obteniendo miembros de la lista:", err);
      setMemberError("No se pudieron obtener los miembros de la lista.");
    }

    setShowMiembrosModal(true);
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
                udsPendientes:
                  updatedProduct.udsPedidas - updatedProduct.udsCompradas, // Cálculo local
              }
            : p
        )
      );
      setShowBuyModal(false);
      setProductError(null); // Limpiar error al éxito
    } catch (err) {
      console.error("Error marcando producto como comprado:", err);
      setProductError("No se pudo marcar el producto como comprado.");
    }
  };

  const handleDeleteMember = async (memberId) => {
    const confirmDelete = window.confirm(
      "¿Estás seguro de que quieres eliminar a este miembro?"
    );

    if (!confirmDelete) return;

    try {
      await listService.deleteMemberFromList(selectedList, memberId);
      alert("Miembro eliminado con éxito.");

      setMiembrosList((prevMembers) =>
        prevMembers.filter((m) => m.usuarioId !== memberId)
      );
      setMemberError(null); // Limpiar error en caso de éxito
    } catch (err) {
      console.error("Error eliminando miembro:", err);
      setMemberError("No se pudo eliminar al miembro.");
    }
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
      const updatedProduct = {
        id: product.id,
        nombre: editProductName,
        udsPedidas: editProductUdsPedidas,
        udsCompradas: editProductUdsCompradas,
        lista: { id: selectedList },
      };

      await listService.editProductFromList(selectedList, updatedProduct);

      setEditProductName("");
      setEditProductUdsPedidas(0);
      setEditProductUdsCompradas(0);
      setShowEditModal(false);
      setProductError(null); // Limpiar error al éxito
    } catch (err) {
      console.error("Error editando producto:", err);
      setProductError("No se pudo editar el producto.");
    }
  };

  const handleCloseEditModal = () => {
    setShowEditModal(false);
  };

  const handleShareListOption = async (option) => {
    if (option === "link") {
      //navigator.clipboard.writeText(shareUrl);
      const url = await listService.generateURL(selectedList);
      setShareUrl(url);
      navigator.clipboard.writeText(url);
      alert("Enlace copiado al portapapeles.");
    } else if (option === "email") {
      if (!inviteEmail.trim()) {
        setShareListError(
          "Por favor, introduce un email para enviar la invitación."
        );
        return;
      }
      handleShareList(); // Enviar la invitación por correo
    }
  };

  // Compartir lista
  const handleShareList = async () => {
    if (!selectedList) {
      setShareListError("Por favor, selecciona una lista antes de compartir.");
      return;
    }

    if (!inviteEmail.trim()) {
      setShareListError(
        "Por favor, introduce un email para compartir la lista."
      );
      return;
    }

    try {
      const url = await listService.shareList(selectedList, inviteEmail); // Pasar emailInvitado
      setShareUrl(url); // Guardar la URL generada
      setInviteSent(true);
      setTimeout(() => setInviteSent(false), 4000);
      setShareListError(null); // Limpiar error en caso de éxito
    } catch (err) {
      console.error("Error compartiendo lista:", err);
      setShareListError("No se pudo compartir la lista.");
    }
  };

  const handleAcceptInvitation = async (invitationId) => {
    try {
      await listService.acceptInvitation(invitationId, "aceptado");
      alert("Invitación aceptada.");
      setError(null); // Limpia notificaciones
      window.location.reload();
    } catch (err) {
      console.error("Error aceptando invitación:", err);
      setError("No se pudo aceptar la invitación.");
    }
  };

  const handleRejectInvitation = async (invitationId) => {
    try {
      await listService.acceptInvitation(invitationId, "rechazado");
      alert("Invitación rechazada.");
      setError(null); // Limpia notificaciones
      window.location.reload();
    } catch (err) {
      console.error("Error rechazando invitación:", err);
      setError("No se pudo rechazar la invitación.");
    }
  };

  const handleOpenShareModal = async (listId) => {
    setSelectedList(listId);
    setInviteEmail("");
    setShareListError(null);
    try {
      const url = await listService.generateURL(listId);
      setShareUrl(url);
    } catch (err) {
      console.error("Error generando enlace de la lista:", err);
      setShareListError("No se pudo generar el enlace de la lista.");
    }
    setShowShareModal(true);
  };

  const handleCloseShareModal = () => {
    setShowShareModal(false);
    setShareUrl("");
  };

  const handleSelectList = async (listId) => {
    ws = listService.desconectarWebSocket(); // Desconectar el WebSocket de la lista anterior

    setSelectedList(listId);
    setProducts([]);
    setShareUrl(""); // Limpiar la URL al cambiar de lista
    document
      .getElementById("productsSection")
      .scrollIntoView({ behavior: "smooth" });

    await fetchProducts(listId); // Carga los productos de la lista seleccionada

    // Conectar el WebSocket de la lista seleccionada
    listService.conectarWebSocket(listId, setProducts);

    // Desplazamiento automático
    const productsSection = document.getElementById("productsSection");
    if (productsSection) {
      productsSection.scrollIntoView({ behavior: "smooth" });
    } else {
      console.error("El elemento con ID 'productsSection' no existe.");
    }
  };

  if (!user) {
    return <p>Cargando datos del usuario...</p>;
  }

  return (
    <div style={styles.container}>
    <header style={styles.header}>
      <h1 style={styles.title}>Tu gestor de listas de la compra</h1>
    </header>

    <div style={styles.rowHeader}>
      <span
        style={{ color: "#4CAF50", fontWeight: "bold", marginRight: "auto" }}
      >
        {user.email}
      </span>

      <div style={{ marginLeft: "auto" }}>
        <button
          onClick={handleLogout}
          style={styles.logoutButton}
          aria-label="Cerrar Sesión"
        >
          Cerrar Sesión <span>&nbsp;</span> <CiLogout />
        </button>
      </div>
    </div>

      <div style={styles.rowHeader}>
        <span
          style={{ color: "#f45000", fontWeight: "bold", marginRight: "auto" }}
        >
          {user.esPagado ? (
            <>
              Premium <MdWorkspacePremium />
            </>
          ) : (
            "Gratuito"
          )}
        </span>
        <div style={{ marginLeft: "auto" }}>
          <button
            onClick={() => setShowChangePasswordForm(true)}
            style={styles.changePasswordButton}
          >
            Cambiar Contraseña <span>&nbsp;</span> <FaExchangeAlt />
          </button>
        </div>
      </div>

      {/* <div style={styles.userInfo}>
        <p>
          <strong>Email:</strong> {user.email}
        </p>
        <button
          onClick={handleLogout}
          style={styles.logoutButton}
          aria-label="Cerrar Sesión"
        >
          Cerrar Sesión <span>&nbsp;</span> <CiLogout />
        </button>
      </div> */}

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
            Cambiar Contraseña <span>&nbsp;</span> <FaExchangeAlt />
          </button>
          <button
            onClick={() => setShowChangePasswordForm(false)}
            style={styles.cancelButton}
          >
            Cancelar
          </button>
        </div>
      ) : null}

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
      <div>
        {pendingInvitations.length > 0 && (
          <div style={styles.invitationsContainer}>
            <h2>Invitaciones Pendientes</h2>
            <table style={styles.invitationTable}>
              <thead>
                <tr>
                  <th style={styles.invitationHeader}>Lista</th>
                  <th style={styles.invitationHeader}>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {pendingInvitations.map((invitation) => (
                  <tr key={invitation.id} style={styles.invitationRow}>
                    <td style={styles.invitationCell}>
                      {invitation.nombreLista || "Sin nombre"}
                    </td>
                    <td style={styles.invitationCell}>
                      <button
                        style={styles.acceptButton}
                        onClick={() => handleAcceptInvitation(invitation.id)}
                      >
                        Aceptar
                      </button>
                      <button
                        style={styles.rejectButton}
                        onClick={() => handleRejectInvitation(invitation.id)}
                      >
                        Rechazar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

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
        {listError && <p style={styles.error}>{listError}</p>}
      </div>

      <ul style={styles.list}>
        {lists.map((list) => (
          <li key={list.id} style={styles.listItem}>
            <span style={styles.listName}>{list.nombre}</span>

            <button
              onClick={() => handleSelectList(list.id)}
              style={styles.selectButton}
              aria-label={`Seleccionar lista ${list.nombre}`}
            >
              Seleccionar
            </button>

            <button
              onClick={() => handleMiembros(list.id)}
              style={styles.memberButton}
              aria-label={`Ver miembros de la lista ${list.nombre}`}
            >
              Miembros <span>&nbsp;</span> <FaUserAlt />
            </button>

            <button
              onClick={() => handleDeleteList(list.id)}
              style={styles.deleteButton}
              aria-label={`Eliminar lista ${list.nombre}`}
            >
              <div
                style={{
                  display: "flex",
                  justifyContent: "center",
                  alignItems: "center",
                }}
              >
                <FaTrashAlt />
              </div>
            </button>

            <button
              onClick={() => handleOpenShareModal(list.id)}
              style={styles.buttonModalCompartir}
              aria-label={`Compartir lista ${list.nombre}`}
            >
              Compartir <span>&nbsp;</span> <FaShareAlt />
            </button>
          </li>
        ))}
      </ul>
      <div id="productsSection" style={styles.productSection}>
        {/* Mostrar lista seleccionada y productos */}
        {selectedList && (
          <>
            <h3 style={styles.productTitle}>
              Productos de la Lista: {selectedList.name}
            </h3>

            {/* Añadir Producto */}
            <div style={styles.addProductContainer}>
              <h4 style={styles.addProductTitle}>Añadir Producto</h4>
              <div style={styles.inputGroup}>
                <input
                  type="text"
                  placeholder="Nombre del producto"
                  value={newProductName}
                  onChange={(e) => setNewProductName(e.target.value)}
                  style={styles.input}
                />
              </div>
              <div style={styles.inputGroup}>
                <input
                  type="number"
                  placeholder="Cantidad pedida"
                  value={newProductQuantity}
                  onChange={(e) =>
                    setNewProductQuantity(Number(e.target.value))
                  }
                  style={styles.input}
                />
              </div>
              <button
                onClick={handleAddProduct}
                style={styles.addProductButton}
              >
                Añadir Producto
              </button>

              {addProductMessage && (
                <p style={styles.successMessage}>{addProductMessage}</p>
              )}
            </div>

            {/* Tabla de Productos */}
            <table style={styles.productTable}>
              <thead>
                <tr>
                  <th style={styles.productTableHeader}>Nombre</th>
                  <th style={styles.productTableHeader}>Uds Pedidas</th>
                  <th style={styles.productTableHeader}>Uds Compradas</th>
                  <th style={styles.productTableHeader}>Uds Pendientes</th>
                  <th style={styles.productTableHeader}>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {products.map((product) => (
                  <tr key={product.id} style={styles.productTableRow}>
                    <td style={styles.productTableCell}>{product.nombre}</td>
                    <td style={styles.productTableCell}>
                      {product.udsPedidas}
                    </td>
                    <td style={styles.productTableCell}>
                      {product.udsCompradas}
                    </td>
                    <td style={styles.productTableCell}>
                      {product.udsPedidas - product.udsCompradas}
                    </td>
                    <td style={styles.productTableCell}>
                      <button
                        style={styles.buyButton}
                        onClick={() => handleBuyProduct(product)}
                        aria-label={`Comprar producto ${product.nombre}`}
                      >
                        Comprar
                      </button>
                      <button
                        style={styles.editButton}
                        onClick={() => handleEditProduct(product)}
                        aria-label={`Editar producto ${product.nombre}`}
                      >
                        Editar
                      </button>
                      <button
                        style={styles.deleteButton}
                        onClick={() => handleDeleteProduct(product)}
                        aria-label={`Eliminar producto ${product.nombre}`}
                      >
                        <FaTrashAlt />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </>
        )}
      </div>

      {showShareModal && (
        <div style={styles.modalOverlay}>
          <div style={styles.modalContent}>
            {/* Botón para cerrar el modal */}
            <button onClick={handleCloseShareModal} style={styles.closeButton}>
              ×
            </button>

            {/* Título del modal */}
            <h3 style={styles.modalTitle}>Opciones para Compartir</h3>

            {/* Compartir por email */}
            <div style={styles.shareContainer}>
              <h4 style={styles.sectionTitle}>Invitar por Email</h4>
              <div style={styles.inputGroup}>
                <input
                  type="email"
                  placeholder="Introduce el email para invitar"
                  value={inviteEmail}
                  onChange={(e) => setInviteEmail(e.target.value)}
                  style={styles.input}
                />
                <button
                  onClick={() => handleShareListOption("email")}
                  style={styles.shareButton}
                >
                  Invitar
                </button>
              </div>
              {inviteSent && (
                <p style={styles.confirmationMessage}>
                  Invitación enviada correctamente a {inviteEmail}.
                </p>
              )}
            </div>

            {/* Generar enlace */}
            <div style={styles.shareContainer}>
              <h4 style={styles.sectionTitle}>Generar Enlace de Compartir</h4>
              <div style={styles.inputGroup}>
                <button
                  onClick={() => handleShareListOption("link")}
                  style={styles.generateButton}
                >
                  Generar y Copiar Enlace
                </button>
              </div>

              {/* Mostrar enlace generado */}
              {shareUrl && (
                <div style={styles.shareUrlContainer}>
                  <p>URL generada:</p>
                  <a
                    href={shareUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    style={styles.shareUrl}
                  >
                    {shareUrl}
                  </a>
                </div>
              )}
            </div>

            {/* Mostrar errores */}
            {shareListError && <p style={styles.error}>{shareListError}</p>}
          </div>
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
              <strong>Unidades Compradas hasta ahora:</strong>{" "}
              {selectedProduct?.udsCompradas}
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

      {showMiembrosModal && (
        <div style={styles.modalOverlay}>
          <div style={styles.modalContent}>
            <h3>Miembros de la Lista</h3>
            <ul>
              {miembrosList.map((miembro) => (
                <li key={miembro.id} style={styles.memberItem}>
                  {miembro.usuarioId}{" "}
                  {miembro.esPropietario && (
                    <span style={{ color: "green" }}>(Propietario)</span>
                  )}
                  {!miembro.esPropietario && (
                    <button
                      onClick={() => handleDeleteMember(miembro.usuarioId)}
                      style={styles.deleteButton}
                    >
                      Eliminar
                    </button>
                  )}
                </li>
              ))}
            </ul>
            <button
              onClick={() => setShowMiembrosModal(false)}
              style={styles.cancelButton}
            >
              Cerrar
            </button>
            {memberError && <p style={styles.error}>{memberError}</p>}
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
    justifyContent: "center", // Center content vertically
  },
  header: {
    marginBottom: "30px",
  },
  title: {
    fontSize: "36px",
    color: "#4CAF50",
    margin: "0",
  },
  userInfo: {
    display: "flex",
    alignItems: "center",
    gap: "10px",
    marginBottom: "10px",
  },
  logoutButton: {
    justifyContent: "center",
    alignItems: "center",
    display: "flex",
    backgroundColor: "#f44336",
    color: "#fff",
    padding: "10px 20px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
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
    justifyContent: "center",
    gap: "10px",
    marginBottom: "20px",
  },
  createListButton: {
    backgroundColor: "#363537", // Naranja para crear lista
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
    justifyContent: "center",
    alignItems: "center",
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
    listStyle: "none",
    padding: "0",
    margin: "0",
    width: "100%",
    maxWidth: "550px",
  },
  listItem: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    padding: "10px",
    borderBottom: "1px solid #ddd",
  },
  listName: {
    flex: "1",
    marginRight: "auto",
  },
  selectButton: {
    backgroundColor: "#4CAF50",
    color: "#fff",
    padding: "5px 10px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
    marginRight: "10px",
  },
  selectedListContainer: {
    marginTop: "20px",
  },
  shareContainer: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    marginTop: "20px",
  },
  shareOptions: {
    display: "flex",
    flexDirection: "row",
    alignItems: "center",
    gap: "10px",
    marginTop: "10px",
    textAlign: "center",
  },
  wideButton: {
    width: "400px",
    padding: "5px 10px",
    height: "35px",
  },
  emailInput: {
    padding: "8px",
    width: "100%",
    maxWidth: "300px",
    border: "1px solid #ccc",
    borderRadius: "5px",
  },
  shareButton: {
    display: "flex",
    alignItems: "center",
    backgroundColor: "#FFC107",
    color: "#000",
    padding: "10px 20px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
    gap: "5px",
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
  productTable: {
    width: "100%",
    borderCollapse: "collapse",
    marginTop: "20px",
    fontFamily: "Arial, sans-serif",
    backgroundColor: "#f9f9f9",
  },
  productTableHeader: {
    backgroundColor: "#4CAF50",
    color: "#fff",
    textAlign: "left",
    padding: "12px 15px",
  },
  productTableCell: {
    padding: "12px 15px",
    textAlign: "left",
    borderBottom: "1px solid #ddd",
  },
  productTableRow: {
    cursor: "pointer",
    borderBottom: "1px solid #ddd",
  },
  productTableRowHover: {
    backgroundColor: "#f1f1f1",
  },
  buyButton: {
    backgroundColor: "#4169e1",
    color: "#fff",
    padding: "5px 10px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
    marginRight: "10px",
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
    justifyContent: "center",
    alignItems: "center",
    display: "flex",
    backgroundColor: "#f44336",
    color: "#fff",
    padding: "5px 10px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
  },
  "@media screen and (max-width: 400px)": {
    productTable: {
      width: "100%",
      fontSize: "10px",
    },
    productTableHeader: {
      padding: "4px 6px",
      fontSize: "10px",
    },
    productTableCell: {
      padding: "4px 6px",
      fontSize: "10px",
    },
    buyButton: {
      padding: "2px 4px",
      fontSize: "5px",
    },
    editButton: {
      padding: "2px 4px",
      fontSize: "5px",
    },
    deleteButton: {
      padding: "2px 4px",
      fontSize: "5px",
    },
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
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
  },
  memberButton: {
    marginLeft: "12px",
    justifyContent: "center",
    alignItems: "center",
    display: "flex",
    backgroundColor: "#363537",
    color: "#fff",
    padding: "5px 10px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
    marginRight: "10px",
  },
  memberItem: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    padding: "8px 0",
  },
  invitationTable: {
    width: "100%",
    borderCollapse: "collapse",
    marginBottom: "20px",
  },
  invitationHeader: {
    backgroundColor: "#4CAF50",
    color: "#fff",
    textAlign: "left",
    padding: "12px 15px",
  },
  invitationRow: {
    borderBottom: "1px solid #ddd",
  },
  invitationCell: {
    padding: "12px 15px",
    textAlign: "left",
  },
  acceptButton: {
    backgroundColor: "#4CAF50",
    color: "#fff",
    padding: "5px 10px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
    marginRight: "5px",
  },
  rejectButton: {
    backgroundColor: "#f44336",
    color: "#fff",
    padding: "5px 10px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
  },
  modalContent: {
    backgroundColor: "#fff",
    padding: "20px",
    borderRadius: "10px",
    boxShadow: "0 4px 6px rgba(0, 0, 0, 0.1)",
    position: "relative",
    width: "90%",
    maxWidth: "600px",
    maxHeight: "80vh",
    overflowY: "auto",
  },
  closeButton: {
    position: "absolute",
    top: "10px",
    right: "10px",
    background: "none",
    border: "none",
    fontSize: "24px",
    cursor: "pointer",
  },
  modalTitle: {
    marginBottom: "20px",
    textAlign: "center",
  },
  shareUrl: {
    color: "#007BFF",
    textDecoration: "underline",
    wordBreak: "break-word",
  },
  inputGroup: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    gap: "10px",
    marginBottom: "15px",
  },
  input: {
    width: "70%",
    padding: "10px",
    borderRadius: "5px",
    border: "1px solid #ccc",
  },
  shareButton: {
    backgroundColor: "#4CAF50",
    color: "#fff",
    padding: "10px 20px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
  },
  generateButton: {
    backgroundColor: "#2196F3",
    color: "#fff",
    padding: "10px 20px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
  },
  error: {
    color: "red",
    marginTop: "10px",
    fontSize: "14px",
  },
  addProductButton: {
    backgroundColor: "#363537",
    color: "#fff",
    padding: "10px 20px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
  },
  buttonModalCompartir: {
    marginLeft: "10px",
    padding: "8px 16px",
    backgroundColor: "#007BFF",
    color: "#fff",
    border: "none",
    borderRadius: "4px",
    cursor: "pointer",
    justifyContent: "center",
    alignItems: "center",
    display: "flex",
  },
  successMessage: {
    color: "green",
    marginTop: "10px",
    fontWeight: "bold",
  },
  confirmationMessage: {
    color: "green",
    marginTop: "10px",
  },
  rowHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    width: "100%",
    maxWidth: "600px",
    marginBottom: "20px",
    backgroundColor: "#f9f9f9",
  },
};

export default UserDashboard;
