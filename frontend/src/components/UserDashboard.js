import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import userService from '../services/userService';

const UserDashboard = () => {
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        const response = await userService.getUserDetails(); // Llama a un endpoint para obtener los datos del usuario logueado
        setUser(response);
        console.log('User info:', response);
      } catch (error) {
        console.error('Error fetching user info:', error);
        navigate('/login'); // Redirige a login si hay un problema con el token
      }
    };

    fetchUserInfo();
  }, [navigate]);

  if (!user) {
    return <p>Cargando datos del usuario...</p>;
  }

  return (
    <div>
      <h1>Bienvenido a tu Dashboard</h1>
      <p><strong>Email:</strong> {user.email || 'No disponible'}</p>
      <p><strong>Tipo de usuario:</strong> {user.esPagado ? 'Premium' : 'Gratuito'}</p>
    </div>
  );
};

export default UserDashboard;
