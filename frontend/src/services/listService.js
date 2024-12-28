import axios from 'axios';

const API_URL = 'http://localhost:80/listas';

const createList = async (listName) => {
    const token = localStorage.getItem('authToken');
    if (!token) {
      throw new Error('Token no encontrado para createList');
    }

    const data = { nombre: listName };

    try {
        console.log('Enviando a la API', data);
        const response = await axios.post(
            `${API_URL}/crearLista`,
            data,
            {
                headers: {
                    token,
                    'Content-Type': 'application/json',
                },
            },
        );
        console.log('Respuesta de la API:', response.data);
        return response.data;
    }
    catch (error) {
        console.error('Error creating list:', error.response?.data || error.message);
        if (error.response && error.response.status === 402) {
          throw new Error('Debes pagar para crear más de 2 listas.');
      }
        throw new Error('No se pudo crear la lista');
    }
};
/*
const getUserLists = async (email) => {
  const response = await axios.get(`${API_URL}/getLista?email=${email}`);
  return response.data;
}; */

const getUserLists = async () => {
    const token = localStorage.getItem('authToken');
    if (!token) {
      throw new Error('Token no encontrado para getUserLists');
    }
    const response = await axios.get(`${API_URL}/getLista`, {
      headers: { token },
    });

    return response.data;
}

const addProductToList = async (listId, product) => {
  const response = await axios.post(`${API_URL}/addProduct`, { listId, product });
  return response.data;
};

export default {
  createList,
  getUserLists,
  addProductToList,
};
