import React, { useState } from 'react';
import userService from '../services/userService';

const LoginForm = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();

    try {
      const token = await userService.login(email, password);
      setMessage('Inicio de sesión exitoso');
      // Guarda el token en el almacenamiento local o cookies
      localStorage.setItem('authToken', token);
    } catch (error) {
      setMessage(`Error: ${error.response?.data?.message || 'Credenciales inválidas'}`);
    }
  };

  return (
    <div style={{ textAlign: 'center', marginTop: '50px' }}>
      <h2>Iniciar Sesión</h2>
      <form onSubmit={handleLogin}>
        <div>
          <label>Email: </label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div>
          <label>Contraseña: </label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit" style={{ marginTop: '10px', padding: '10px 20px' }}>
          Iniciar Sesión
        </button>
      </form>
      {message && <p style={{ color: 'red' }}>{message}</p>}
    </div>
  );
};

export default LoginForm;
