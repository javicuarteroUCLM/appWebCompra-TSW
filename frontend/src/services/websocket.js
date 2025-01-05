/** @format */

import { w3cwebsocket as WebSocket } from "websocket";

let ws; // Variable para mantener la conexión WebSocket
const subscriptions = {}; // Almacenar callbacks para listas específicas

// Conectar al WebSocket
/*
export const connectWebSocket = (email, selectedList, setProducts) => {
  if (ws && ws.readyState === WebSocket.OPEN) {
    console.log("WebSocket ya conectado.");
    return;
  }

  //const wsUrl = `ws://localhost:8383/wsListas?email=${email}`;
  const wsUrl = `ws://localhost:8383/wsListas?token=${localStorage.getItem('authToken')}`;
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
  }; 
  ws.onmessage = (message) => {
    try {
      const data = JSON.parse(message.data);
      console.log("Mensaje recibido por WebSocket:", data);
      console.log("Lista seleccionada:", selectedList);
      console.log("Id de lista en mensaje:", data.idLista);

      if (
        data.type === "actualizacionDeLista" &&
        data.idLista === selectedList
      ) {
        console.log("Actualización de lista recibida:", data.action);
        switch (data.action) {
          case "updateProduct":
            console.log("Actualizando producto...");
            console.log("Productos actuales:", setProducts);
            console.log("Producto actualizado:", data.producto);
            setProducts((prevProducts) =>
              prevProducts.map((p) =>
                p.id === data.producto.id ? data.producto : p
              )
            );
            break;

          case "deleteProduct":
            console.log("Eliminando producto en tiempo real:", data.idProducto);
            setProducts((prevProducts) =>
              prevProducts.filter((p) => p.id !== data.idProducto)
            );
            break;

          case "addProduct":
            console.log("Agregando producto...");
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
}; */

export const connectWebSocket = (selectedList, setProducts) => {

  if (ws && ws.readyState === WebSocket.OPEN) {
    console.log("Cerrando conexión WebSocket anterior...");
    ws.close();
  }

  const token = sessionStorage.getItem("authToken");
  const wsUrl = `ws://localhost:8383/wsListas?token=${token}`;

  ws = new WebSocket(wsUrl);

  ws.onopen = () => {
    console.log("Conexión WebSocket para usuario con token:" +token+ " para la lista:", selectedList);
  };

  ws.onmessage = (message) => {
    try {
      const data = JSON.parse(message.data);
      if (data.type === "actualizacionDeLista" && data.idLista === selectedList) {

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
          // Otros casos
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
export const getWebSocket = (email, selectedList, setProducts) => {
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
