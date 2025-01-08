/** @format */

import { w3cwebsocket as WebSocket } from "websocket";
import { API_URL_LISTAS_WS } from "../environments/commonst";

let ws; // Variable para mantener la conexión WebSocket

// Conectar al WebSocket
export const connectWebSocket = (selectedList, setProducts) => {
  if (ws && ws.readyState === WebSocket.OPEN) {
    console.log("Cerrando conexión WebSocket anterior...");
    ws.close();
  }

  const token = sessionStorage.getItem("authToken");
  const wsUrl = `${API_URL_LISTAS_WS}?token=${token}`;

  ws = new WebSocket(wsUrl);

  ws.onopen = () => {
    console.log(
      "Conexión WebSocket para usuario con token:" + token + " para la lista:",
      selectedList
    );
  };

  ws.onmessage = (message) => {
    try {
      const data = JSON.parse(message.data);
      if (
        data.type === "actualizacionDeLista" &&
        data.idLista === selectedList
      ) {
        switch (data.action) {
          case "updateProduct":
            setProducts((prevProducts) =>
              prevProducts.map((p) =>
                p.id === data.producto.id ? data.producto : p
              )
            );
            break;

          case "deleteProduct":
            setProducts((prevProducts) =>
              prevProducts.filter((p) => p.id !== data.idProducto)
            );
            break;

          case "addProduct":
            setProducts((prevProducts) => [...prevProducts, data.producto]);
            break;

          default:
            console.warn("Acción no reconocida:", data.action);
        }
      }
    } catch (err) {
      console.error("Error procesando mensaje WebSocket:", err);
    }
  };

  ws.onclose = () => {
    console.log("Conexión WebSocket cerrada");
  };

  ws.onerror = (error) => {
    console.error("Error en WebSocket:", error);
  };
};

// Funcion para conectar al WebSocket y devolverlo
export const getWebSocket = (selectedList, setProducts) => {
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    connectWebSocket(selectedList, setProducts);
  }
  return ws;
};

// Enviar un mensaje a través del WebSocket (si es necesario para acciones específicas)
export const sendMessage = (message) => {
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    console.error("WebSocket no está conectado.");
    return;
  }

  ws.send(JSON.stringify(message));
};

export default {
  sendMessage,
  getWebSocket,
  connectWebSocket,
};
