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

  const wsUrl = `ws://localhost:8383/wsListas?email=${email}`;
  ws = new WebSocket(wsUrl);

  ws.onopen = () => {
    console.log("Conexión WebSocket establecida para el email:", email);
  };

  ws.onclose = () => {
    console.log("Conexión WebSocket cerrada. Intentando reconectar...");
    ws = null;
  };

  ws.onerror = (error) => {
    console.error("Error en WebSocket:", error);
  };

  /*
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
  }; */
  ws.onmessage = (message) => {
    try {
      const data = JSON.parse(message.data);
      console.log("Mensaje recibido por WebSocket:", data);
      console.log("Productos:", data.productos);

      if (data.type === "actualizacionDeLista") {
        const listId = data.idLista;

        // Verifica si hay una suscripción activa para la lista
        if (subscriptions[listId]) {
          const callback = subscriptions[listId];

          switch (data.action) {
            case "updateProduct":
              const { producto } = data;
              console.log("Actualizando producto:", producto);

              // Actualizar o agregar el producto en la lista
              callback((prevProducts) => {
                const existingProduct = prevProducts.find(
                  (p) => p.id === data.producto.id
                );

                if (existingProduct) {
                  // Si el producto ya existe, actualiza su información
                  return prevProducts.map((p) =>
                    p.id === data.producto.id ? data.producto : p
                  );
                } else {
                  // Si no existe, agrega el nuevo producto
                  return [...prevProducts, data.producto];
                }
              });
              break;

            case "deleteProduct":
              // Eliminar el producto de la lista
              callback((prevProducts) =>
                prevProducts.filter((p) => p.id !== data.idProducto)
              );
              break;

            case "editProduct":
              callback((prevProducts) =>
                prevProducts.map((p) =>
                  p.id === data.producto.id ? data.producto : p
                )
              );
              break;

            case "buyProduct":
              callback((prevProducts) =>
                prevProducts.map((p) =>
                  p.id === data.producto.id ? data.producto : p
                )
              );
              break;

            case "newList":
              // Manejar una nueva lista compartida (si aplica)
              console.log("Nueva lista recibida:", data.listDetails);
              break;
            case "addProduct":
              callback((prevProducts) => [...prevProducts, data.producto]);
              break;

            default:
              console.warn("Acción no reconocida en WebSocket:", data.action);
          }
        }
      }
    } catch (err) {
      console.error("Error procesando mensaje WebSocket:", err);
    }
  };
};

// Funcion para conectar al WebSocket y devolverlo
export const getWebSocket = (email) => {
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    connectWebSocket(email);
  }
  return ws;
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
  subscribeToListUpdates,
  unsubscribeFromListUpdates,
  sendMessage,
};
