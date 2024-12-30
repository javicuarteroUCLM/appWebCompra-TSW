/** @format */

import React, { useState } from "react";
import userService from "../services/userService";
import { FaEnvelope, FaLock, FaEye, FaEyeSlash } from "react-icons/fa";
import { useNavigate } from "react-router-dom";

const RegisterForm = () => {
  const [email, setEmail] = useState("");
  const [password1, setPassword1] = useState("");
  const [password2, setPassword2] = useState("");
  const [message, setMessage] = useState("");
  const [showPassword1, setShowPassword1] = useState(false);
  const [showPassword2, setShowPassword2] = useState(false);
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();

    if (password1 !== password2) {
      setMessage("Las contraseñas no coinciden");
      return;
    }

    try {
      // Realiza el registro y obtén el token
      const token = await userService.register(email, password1, password2);
      setMessage("Registro exitoso. Bienvenido.");
      localStorage.setItem("authToken", token); // Almacena el token en el almacenamiento local
      //navigate('/dashboard'); // Redirige al dashboard
    } catch (error) {
      setMessage(`Error: ${error.response?.data?.message || "Algo salió mal"}`);
    }
  };

  const togglePasswordVisibility1 = () => {
    setShowPassword1((prevShowPassword) => !prevShowPassword);
  };

  const togglePasswordVisibility2 = () => {
    setShowPassword2((prevShowPassword) => !prevShowPassword);
  };

  return (
    <div style={styles.container}>
      <div style={styles.formContainer}>
        <h2 style={styles.title}>Registrarse</h2>
        <form onSubmit={handleRegister} style={styles.form}>
          <div style={styles.inputGroup}>
            <FaEnvelope style={styles.icon} />
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Correo Electrónico"
              style={styles.input}
              required
            />
          </div>
          <div style={styles.inputGroup}>
            <FaLock style={styles.icon} />
            <input
              type={showPassword1 ? "text" : "password"}
              value={password1}
              onChange={(e) => setPassword1(e.target.value)}
              placeholder="Contraseña"
              style={styles.input}
              required
            />
            <button
              type="button"
              onClick={togglePasswordVisibility1}
              style={styles.eyeButton}
            >
              {showPassword1 ? <FaEyeSlash /> : <FaEye />}
            </button>
          </div>
          <div style={styles.inputGroup}>
            <FaLock style={styles.icon} />
            <input
              type={showPassword2 ? "text" : "password"}
              value={password2}
              onChange={(e) => setPassword2(e.target.value)}
              placeholder="Repite la Contraseña"
              style={styles.input}
              required
            />
            <button
              type="button"
              onClick={togglePasswordVisibility2}
              style={styles.eyeButton}
            >
              {showPassword2 ? <FaEyeSlash /> : <FaEye />}
            </button>
          </div>
          <button type="submit" style={styles.button}>
            Registrarse
          </button>
        </form>
        {message && <p style={styles.message}>{message}</p>}
      </div>
    </div>
  );
};

const styles = {
  container: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    minHeight: "100vh",
    backgroundColor: "#f7f9fb",
    padding: "20px",
  },
  formContainer: {
    width: "100%",
    maxWidth: "400px",
    backgroundColor: "#fff",
    borderRadius: "10px",
    boxShadow: "0 4px 8px rgba(0, 0, 0, 0.1)",
    padding: "30px",
    textAlign: "center",
  },
  title: {
    fontSize: "24px",
    fontWeight: "bold",
    marginBottom: "20px",
    color: "#4CAF50",
  },
  form: {
    display: "flex",
    flexDirection: "column",
    gap: "15px",
  },
  inputGroup: {
    display: "flex",
    alignItems: "center",
    backgroundColor: "#f0f0f0",
    borderRadius: "5px",
    padding: "10px",
    position: "relative",
  },
  icon: {
    marginRight: "10px",
    color: "#4CAF50",
  },
  input: {
    border: "none",
    outline: "none",
    flex: 1,
    backgroundColor: "transparent",
    fontSize: "16px",
    color: "#333",
  },
  eyeButton: {
    backgroundColor: "transparent",
    border: "none",
    cursor: "pointer",
    color: "#4CAF50",
    position: "absolute",
    right: "10px",
    fontSize: "16px",
  },
  button: {
    backgroundColor: "#ff9800",
    color: "#fff",
    fontSize: "16px",
    padding: "12px 20px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
    transition: "background-color 0.3s",
  },
  message: {
    marginTop: "15px",
    fontSize: "14px",
    color: "#ff5722",
  },
};

export default RegisterForm;
