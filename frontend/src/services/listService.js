/** @format */

import axios from "axios";
import { sendMessage, connectWebSocket } from "./websocket";
import { getUserDetails } from "./userService";

const API_URL = "http://localhost:8383/listas";
let ws;

// Crear una Lista
const createList = async (listName) => {
  const token = localStorage.getItem("authToken");
  if (!token) {
    throw new Error("Token no encontrado para createList");
  }

  const data = { nombre: listName };

  try {
    const response = await axios.post(`${API_URL}/crearLista`, data, {
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

// Obtener Listas del Usuario
const getUserLists = async () => {
  const token = localStorage.getItem("authToken");
  if (!token) {
    throw new Error("Token no encontrado para getUserLists");
  }

  const response = await axios.get(`${API_URL}/getListas`, {
    withCredentials: true,
    headers: {
      token: token,
      "Content-Type": "application/json",
    },
  });

  return response.data;
};

// Añadir Producto a Lista
const addProductToList = async (listId, product) => {
  const token = localStorage.getItem("authToken");
  if (!token) {
    throw new Error("Token no encontrado.");
  }

  console.log("Añadiendo producto a la lista", listId, product);

  const response = await axios.post(`${API_URL}/addProducto`, product, {
    headers: {
      token,
      idLista: listId,
      "Content-Type": "application/json",
    },
  });

  const userDetails = await getUserDetails();
  const email = userDetails.email;

  // Reconectar WebSocket si no está conectado
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    console.log("Reconectando WebSocket...");
    connectWebSocket(email);
  }

  const message = {
    type: "actualizacionDeLista",
    idLista: listId,
    producto: {
      id: product.id,
      nombre: product.nombre,
      udsPedidas: product.udsPedidas,
      udsCompradas: product.udsCompradas,
    },
  };
  sendMessage(message);

  return response.data;
};

// Obtener Productos de una Lista
const getProductsByListId = async (listId) => {
  if (!listId) {
    throw new Error("No se ha proporcionado un ID de lista.");
  }

  const response = await axios.get(`${API_URL}/productos/${listId}`);

  return response.data;
};

const listService = {
  createList,
  getUserLists,
  addProductToList,
  getProductsByListId,
};
export default listService;
