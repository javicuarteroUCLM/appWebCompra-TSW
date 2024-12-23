import React from 'react';
import { useNavigate } from 'react-router-dom';

const LandingPage = () => {
  const navigate = useNavigate();

  const handleRegister = () => {
    navigate('/register');
  };

  const handleLogin = () => {
    navigate('/login');
  };

  return (
    <div style={{ textAlign: 'center', marginTop: '100px' }}>
      <h1>Bienvenido a la Aplicación de Listas Compartidas</h1>
      <button onClick={handleLogin} style={{ margin: '10px', padding: '10px 20px' }}>Iniciar Sesión</button>
      <button onClick={handleRegister} style={{ margin: '10px', padding: '10px 20px' }}>Registrarse</button>
    </div>
  );
};

export default LandingPage;
