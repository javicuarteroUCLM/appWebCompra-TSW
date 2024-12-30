/** @format */

import { w3cwebsocket as WebSocket } from "websocket";

let ws; // Variable para mantener la conexión WebSocket
const subscriptions = {}; // Almacenar callbacks para listas específicas

// Conectar al WebSocket
export const connectWebSocket = (email) => {
  if (ws && ws.readyState === WebSocket.OPEN) {
    console.log("WebSocket ya conectado.");
    return;
  }

  const wsUrl = `wss://localhost:8383/wsListas?email=${encodeURIComponent(
    email
  )}`;
  ws = new WebSocket(wsUrl);

  ws.onopen = () => {
    console.log("Conexión WebSocket establecida.");
  };

  ws.onclose = () => {
    console.log("Conexión WebSocket cerrada.");
    ws = null;
  };

  ws.onerror = (error) => {
    console.error("Error en WebSocket:", error);
  };

  ws.onmessage = (message) => {
    try {
      const data = JSON.parse(message.data);

      if (data.type === "actualizacionDeLista") {
        const listId = data.idLista;
        if (subscriptions[listId]) {
          subscriptions[listId](data); // Llama al callback asociado
        }
      }
    } catch (err) {
      console.error("Error procesando mensaje WebSocket:", err);
    }
  };
};

// Suscribirse a actualizaciones de una lista específica
export const subscribeToListUpdates = (listId, callback) => {
  subscriptions[listId] = callback;
};

// Cancelar suscripción a actualizaciones de una lista específica
export const unsubscribeFromListUpdates = (listId) => {
  delete subscriptions[listId];
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
  connectWebSocket,
  subscribeToListUpdates,
  unsubscribeFromListUpdates,
  sendMessage,
};
