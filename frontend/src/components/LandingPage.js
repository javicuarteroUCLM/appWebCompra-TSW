import React from 'react';
import { useNavigate } from 'react-router-dom';

// Importa algunos iconos para los botones
import { FaShoppingCart, FaUser } from 'react-icons/fa';

const LandingPage = () => {
  const navigate = useNavigate();

  const handleRegister = () => {
    navigate('/register');
  };

  const handleLogin = () => {
    navigate('/login');
  };

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h1 style={styles.title}>Bienvenido a la Aplicación de Listas Compartidas</h1>
        <p style={styles.subtitle}>¡Haz tus compras más fáciles y compartidas con amigos y familiares!</p>
      </div>
      
      <div style={styles.buttonContainer}>
        <button onClick={handleLogin} style={styles.button}>
          <FaUser style={styles.icon} /> Iniciar Sesión
        </button>
        <button onClick={handleRegister} style={styles.button}>
          <FaUser style={styles.icon} /> Registrarse
        </button>
      </div>

      <div style={styles.footer}>
        <img src="https://img.icons8.com/ios/452/shopping-cart.png" alt="supermarket" style={styles.cartImage} />
      </div>
    </div>
  );
};

const styles = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center',
    textAlign: 'center',
    minHeight: '100vh',
    backgroundColor: '#f7f9fb', // Color de fondo suave
    padding: '20px',
  },
  header: {
    marginBottom: '40px',
  },
  title: {
    fontSize: '36px',
    fontWeight: 'bold',
    color: '#4CAF50', // Verde fresco
    margin: '0',
  },
  subtitle: {
    fontSize: '18px',
    color: '#666',
    marginTop: '10px',
  },
  buttonContainer: {
    display: 'flex',
    justifyContent: 'center',
    gap: '20px',
  },
  button: {
    backgroundColor: '#ff9800', // Naranja fresco
    color: '#fff',
    fontSize: '16px',
    padding: '12px 24px',
    border: 'none',
    borderRadius: '5px',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    transition: 'background-color 0.3s',
  },
  buttonHover: {
    backgroundColor: '#ff5722',
  },
  icon: {
    fontSize: '20px',
  },
  footer: {
    marginTop: '50px',
  },
  cartImage: {
    width: '80px',
    height: '80px',
  },
};

export default LandingPage;
