import axios from 'axios';

const API_URL = 'http://localhost:9000'; // URL de la API de usuarios (backend)
const API_URL_USERS = 'http://localhost:9000/users'; // URL de la API de usuarios (backend)
const API_URL_PAGOS = 'http://localhost:9000/pagos'; // URL de la API de pagos (backend)
const API_URL_LISTAS = 'http://localhost:80/listas';


const register = async (email, pwd1, pwd2) => {
  const params = new URLSearchParams();
  params.append('email', email);
  params.append('pwd1', pwd1);
  params.append('pwd2', pwd2);

  await axios.get(`${API_URL_USERS}/registrar2`, { params });
};

const login = async (email, password) => {
    const response = await axios.put(
      `${API_URL_USERS}/login1`,
      { email, pwd: password }, // Envía los datos en el cuerpo de la solicitud
      { withCredentials: true } // Habilita las cookies para el token
    );
    return response.data; // Devuelve el token
  };

  const prepareTransaction = async (amount) => {
    const response = await axios.put(`${API_URL_PAGOS}/prepararTransaccion`, amount, {
      headers: { 'Content-Type': 'application/json' },
    });
    return response.data; // Devuelve el client_secret
  };

  const getUserDetails = async () => {
    const token = localStorage.getItem('authToken');
    const response = await axios.get(`${API_URL}/users/details`, {
      headers: { token }, // Enviar el token del usuario
    });
    return response.data; // Devuelve los detalles del usuario
  };

  const getLists = async () => {
    const token = localStorage.getItem('authToken');
    const response = await axios.get(`${API_URL_LISTAS}/getLista`, {
      headers: { token },
    });
    return response.data; // Devuelve las listas
  };
  
  const createList = async (listName) => {
    const token = localStorage.getItem('authToken'); // Recupera el token
    const data = { nombre: listName }; // En formato JSON
  
    try {
      const response = await axios.post(
        'http://localhost:80/listas/crearLista',
        data, // Envía un objeto JSON como cuerpo
        {
          headers: {
            token, // Manda token en el header
            'Content-Type': 'application/json',
          },
        }
      );
      console.log(response.data);
      return response.data; // Devuelve la lista creada
    } catch (error) {
      console.error('Error al crear la lista:', error.response.data);
      throw error;
    }
  };
  
  
  

  const addProduct = async (listId, product) => {
    const token = localStorage.getItem('authToken');
    const response = await axios.post(`${API_URL_LISTAS}/addProducto`, product, {
      headers: { token, idLista: listId },
    });
    return response.data; // Devuelve la lista actualizada
  };

export default {
  register,
  login,
  prepareTransaction,
  getUserDetails,
  createList,
  getLists,
  addProduct,
};
