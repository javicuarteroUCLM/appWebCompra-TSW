import React, { useState } from 'react';
import userService from '../services/userService';

const RegisterForm = () => {
  const [email, setEmail] = useState('');
  const [password1, setPassword1] = useState('');
  const [password2, setPassword2] = useState('');
  const [message, setMessage] = useState('');

  const handleRegister = async (e) => {
    e.preventDefault();

    if (password1 !== password2) {
      setMessage('Las contraseñas no coinciden');
      return;
    }

    try {
      await userService.register(email, password1, password2);
      setMessage('Registro exitoso. Por favor, inicia sesión.');
    } catch (error) {
      setMessage(`Error: ${error.response?.data?.message || 'Algo salió mal'}`);
    }
  };

  return (
    <div style={{ textAlign: 'center', marginTop: '50px' }}>
      <h2>Registrarse</h2>
      <form onSubmit={handleRegister}>
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
            value={password1}
            onChange={(e) => setPassword1(e.target.value)}
            required
          />
        </div>
        <div>
          <label>Repite la Contraseña: </label>
          <input
            type="password"
            value={password2}
            onChange={(e) => setPassword2(e.target.value)}
            required
          />
        </div>
        <button type="submit" style={{ marginTop: '10px', padding: '10px 20px' }}>
          Registrarse
        </button>
      </form>
      {message && <p style={{ color: 'red' }}>{message}</p>}
    </div>
  );
};

export default RegisterForm;
