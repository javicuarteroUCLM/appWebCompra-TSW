import axios from 'axios';

const API_URL = 'http://localhost:9000/users'; // URL de la API de usuarios (backend)

const register = async (email, pwd1, pwd2) => {
  const params = new URLSearchParams();
  params.append('email', email);
  params.append('pwd1', pwd1);
  params.append('pwd2', pwd2);

  await axios.get(`${API_URL}/registrar2`, { params });
};

const login = async (email, password) => {
    const response = await axios.put(
      `${API_URL}/login1`,
      { email, pwd: password }, // Envía los datos en el cuerpo de la solicitud
      { withCredentials: true } // Habilita las cookies para el token
    );
    return response.data; // Devuelve el token
  };

export default {
  register,
  login,
};
