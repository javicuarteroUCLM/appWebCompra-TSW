import axios from 'axios';

const API_URL = 'http://localhost:9000/users'; // URL de la API de usuarios (backend)

// Registrar un nuevo usuario
const register = async (email, pwd1, pwd2) => {
  const params = new URLSearchParams();
  params.append('email', email);
  params.append('pwd1', pwd1);
  params.append('pwd2', pwd2);

  await axios.get(`${API_URL}/registrar2`, { params });
};

// Iniciar sesión y obtener el token
const login = async (email, password) => {
  const response = await axios.put(
    `${API_URL}/login1`,
    { email, pwd: password }, // Datos enviados en el cuerpo de la solicitud
    { withCredentials: true } // Habilita las cookies para el token
  );
  // Guardar el token en localStorage
  const token = response.data;
  localStorage.setItem('authToken', token);
  return token; // Retorna el token para confirmación
};

// Obtener detalles del usuario logueado
const getUserDetails = async () => {
  const token = localStorage.getItem('authToken');
  if (!token) {
    throw new Error('Token no encontrado');
  }
  const response = await axios.get(`http://localhost:9000/tokens/obtenerEmail`, {
    params: { token },
  });
  return { email: response.data, esPagado: false };
};

export default {
  register,
  login,
  getUserDetails,
};
