/** @format */

import axios from "axios";
import { sendMessage, getWebSocket } from "./websocket";
import { API_URL_LISTAS_LISTAS } from "../environments/commonst";

let ws;

// Crear una Lista
const createList = async (listName) => {
  const token = sessionStorage.getItem("authToken");
  const data = { nombre: listName };

  try {
    const response = await axios.post(`${API_URL_LISTAS}/crearLista`, data, {
      headers: {
        token,
        "Content-Type": "application/json",
      },
    });
    return response.data;
  } catch (error) {
    console.error(
      "Error creating list:",
      error.response?.data || error.message
    );
    if (error.response && error.response.status === 402) {
      throw new Error("Debes pagar para crear más de 2 listas.");
    }
    throw new Error("No se pudo crear la lista");
  }
};

const deleteList = async (listId) => {
  const token = sessionStorage.getItem("authToken");
  try {
    await axios.delete(`${API_URL_LISTAS}/borrarLista`, {
      headers: {
        token: token,
        idLista: listId,
        "Content-Type": "application/json",
      },
    });
  } catch (err) {
    if (
      err.response.data.message ===
        "No tienes permiso para borrar esta lista" &&
      err.response.status === 403
    ) {
      throw new Error("Solo el propietario puede eliminar la lista.");
    }

    throw new Error("No pudo eliminar la lista.");
  }
};

// Obtener Listas del Usuario
const getUserLists = async () => {
  const token = sessionStorage.getItem("authToken");
  const response = await axios.get(`${API_URL_LISTAS}/getListas`, {
    withCredentials: true,
    headers: {
      token: token,
      "Content-Type": "application/json",
    },
  });

  return response.data;
};

// Compartir Lista
const shareList = async (listId, emailInvitado) => {
  const response = await fetch(`${API_URL_LISTAS}/compartirLista`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      idLista: listId,
      token: sessionStorage.getItem("authToken"),
    },
    body: JSON.stringify({ emailInvitado }), // Enviar email como JSON
  });
  if (!response.ok) {
    throw new Error("Error compartiendo lista");
  }
  return await response.text(); // La respuesta contiene la URL generada
};

const generateURL = async (listId) => {
  const response = await fetch(`${API_URL_LISTAS}/generarURL`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      idLista: listId,
      token: sessionStorage.getItem("authToken"),
    },
  });
  if (!response.ok) {
    throw new Error("Error generando URL");
  }
  return await response.text(); // La respuesta contiene la URL generada
};

// Añadir Producto a Lista
const addProductToList = async (listId, product) => {
  const token = sessionStorage.getItem("authToken");

  const response = await axios.post(`${API_URL_LISTAS}/addProducto`, product, {
    headers: {
      token,
      idLista: listId,
      "Content-Type": "application/json",
    },
  });

  // Mandar difusion por Websocket
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    console.log("WebSocket no conectado. No se enviará mensaje.");
    conectarWebSocket();
    await new Promise((resolve) => setTimeout(resolve, 1000));
  } else {
    console.log("WebSocket conectado.");
  }

  const message = {
    type: "actualizacionDeLista",
    action: "addProduct",
    idLista: listId,
    producto: {
      id: response.data,
      nombre: product.nombre,
      udsPedidas: product.udsPedidas,
      udsCompradas: product.udsCompradas,
    },
  };

  // No mandar mensaje ya que ahora se encarga el Servidor de mandar la difusion
  //sendMessage(message);

  return response.data;
};

// Eliminar Producto de Lista
const deleteProductFromList = async (productId) => {
  try {
    await axios.delete(`${API_URL_LISTAS}/eliminarProducto`, {
      headers: {
        idProducto: productId,
        "Content-Type": "application/json",
      },
    });
  } catch (err) {
    console.error("Error eliminando producto:", err.response?.data || err);
    throw new Error("No se pudo eliminar el producto.");
  }
};

// Editar Producto de Lista
const editProductFromList = async (selectedList, product) => {
  try {
    await axios.put(`${API_URL_LISTAS}/editarProducto`, product, {
      headers: {
        "Content-Type": "application/json",
      },
    });
  } catch (err) {
    console.error("Error editando producto:", err.response?.data || err);
    throw new Error("No se pudo editar el producto.");
  }

  // Mandar difusion por Websocket
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    console.log("WebSocket no conectado. No se enviará mensaje.");
    conectarWebSocket();
    await new Promise((resolve) => setTimeout(resolve, 1000));
  } else {
    console.log("WebSocket conectado. Enviando mensaje.");
  }

  const message = {
    type: "actualizacionDeLista",
    action: "updateProduct",
    idLista: selectedList,
    producto: {
      id: product.id,
      nombre: product.nombre,
      udsPedidas: product.udsPedidas,
      udsCompradas: product.udsCompradas,
    },
  };

  //sendMessage(message);

  return product;
};

const buyProduct = async (productId, udsCompradas) => {
  try {
    const response = await axios.put(
      `${API_URL_LISTAS}/comprarProducto`,
      { idProducto: productId, udsCompradas },
      { headers: { "Content-Type": "application/json" } }
    );
    return response.data;
  } catch (err) {
    console.error(
      "Error marcando producto como comprado:",
      err.response?.data || err
    );
    throw new Error("No se pudo marcar el producto como comprado.");
  }
};

const getMiembros = async (listId) => {
  const response = await axios.get(`${API_URL_LISTAS}/miembros`, {
    headers: {
      idLista: listId,
      "Content-Type": "application/json",
    },
  });

  return response.data;
};

// Obtener Productos de una Lista
const getProductsByListId = async (listId) => {
  if (!listId) {
    throw new Error("No se ha proporcionado un ID de lista.");
  }
  const response = await axios.get(`${API_URL_LISTAS}/productos/${listId}`);

  return response.data;
};

const deleteMemberFromList = async (listId, email) => {
  try {
    const token = sessionStorage.getItem("authToken");
    await axios.delete(`${API_URL_LISTAS}/borrarUsuarioDeLista`, {
      headers: {
        idLista: listId,
        token: token,
        "Content-Type": "application/json",
      },
      data: email,
    });
  } catch (err) {
    if (
      err.response.data.message ===
        "No tienes permiso para borrar este miembro" &&
      err.response.status === 403
    ) {
      throw new Error("Solo el propietario puede eliminar miembros.");
    }

    throw new Error("No pudo eliminar el miembro.");
  }
};

const getInvitations = async () => {
  const token = sessionStorage.getItem("authToken");
  const response = await axios.get(`${API_URL_LISTAS}/invitaciones`, {
    headers: {
      token: token,
      "Content-Type": "application/json",
    },
  });

  return response.data;
};

const acceptInvitation = async (invitationId, status) => {
  const response = await fetch(`${API_URL_LISTAS}/aceptarInvitacion`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      idInvitacion: invitationId, // ID de la invitación
    },
    body: JSON.stringify(status.toLowerCase()), // Convertir estado a minúsculas
  });
  console.log(response);
  if (!response.ok) {
    throw new Error("Error aceptando/rechazando invitación");
  }
  if (
    response.headers.get("content-length") !== "0" &&
    response.headers.get("content-type")?.includes("application/json")
  ) {
    return await response.json();
  }
  return { message: "Operación realizada correctamente" };
};

// Conectar WebSocket
const conectarWebSocket = async (selectedList, setProducts) => {
  ws = getWebSocket(selectedList, setProducts);
  return ws;
};

const desconectarWebSocket = () => {
  if (ws) {
    ws.close();
    ws = null;
  }
};

const listService = {
  createList,
  getUserLists,
  addProductToList,
  getProductsByListId,
  conectarWebSocket,
  desconectarWebSocket,
  shareList,
  deleteProductFromList,
  editProductFromList,
  buyProduct,
  deleteList,
  getMiembros,
  deleteMemberFromList,
  getInvitations,
  acceptInvitation,
  generateURL,
};
export default listService;
