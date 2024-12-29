/** @format */

import axios from "axios";
import { sendMessage, connectWebSocket } from "./websocket"; // Importa la función sendMessage y connectWebSocket desde websocket.js
import { getUserDetails } from "./userService";

const API_URL = "http://localhost:80/listas";
let ws;

const createList = async (listName) => {
  const token = localStorage.getItem("authToken");
  if (!token) {
    throw new Error("Token no encontrado para createList");
  }

  const data = { nombre: listName };

  try {
    console.log("Enviando a la API", data);
    const response = await axios.post(`${API_URL}/crearLista`, data, {
      headers: {
        token,
        "Content-Type": "application/json",
      },
    });
    console.log("Respuesta de la API:", response.data);
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

const getUserLists = async () => {
  const token = localStorage.getItem("authToken");
  if (!token) {
    throw new Error("Token no encontrado para getUserLists");
  }
  const response = await axios.get(`${API_URL}/getListas`, {
    headers: { token },
  });

  return response.data;
};

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

  // Pide email a traves de token
  const userDetails = await getUserDetails();
  const email = userDetails.email;
  console.log("Email del usuario:", email);
  // Asegurarse de que el WebSocket esté conectado antes de enviar el mensaje
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    console.error("WebSocket no está conectado. Intentando reconectar...");
    ws = connectWebSocket(email);
  }

  // Enviar notificación a través del WebSocket
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

const getProductsByListId = async (listId) => {
  const token = localStorage.getItem("authToken");
  if (!token) {
    throw new Error("Token no encontrado.");
  }

  const response = await axios.get(`${API_URL}/getProductos`, {
    headers: { token, idLista: listId },
  });

  return response.data;
};

const listService = {
  createList,
  getUserLists,
  addProductToList,
  getProductsByListId,
};
export default listService;
