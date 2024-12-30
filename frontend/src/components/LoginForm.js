/** @format */

import React, { useState } from "react";
import userService from "../services/userService";
import { useNavigate } from "react-router-dom";
import { FaEnvelope, FaLock, FaEye, FaEyeSlash } from "react-icons/fa";

const LoginForm = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();

    try {
      const token = await userService.login(email, password);
      setMessage("Inicio de sesión exitoso");
      localStorage.setItem("authToken", token);
      navigate("/dashboard");
    } catch (error) {
      setMessage(
        `Error: ${error.response?.data?.message || "Credenciales inválidas"}`
      );
    }
  };

  const togglePasswordVisibility = () => {
    setShowPassword((prevShowPassword) => !prevShowPassword);
  };

  return (
    <div style={styles.container}>
      <div style={styles.formContainer}>
        <h2 style={styles.title}>Iniciar Sesión</h2>
        <form onSubmit={handleLogin} style={styles.form}>
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
              type={showPassword ? "text" : "password"}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Contraseña"
              style={styles.input}
              required
            />
            <button
              type="button"
              onClick={togglePasswordVisibility}
              style={styles.eyeButton}
            >
              {showPassword ? <FaEyeSlash /> : <FaEye />}
            </button>
          </div>
          <div style={styles.forgotPassword}>
            <button
              type="button"
              onClick={() => navigate("/reset-password")}
              style={styles.forgot}
            >
              ¿Olvidaste tu contraseña?
            </button>
          </div>
          <button type="submit" style={styles.button}>
            Iniciar Sesión
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
  forgotPassword: {
    textAlign: "right",
  },
  forgot: {
    backgroundColor: "transparent",
    border: "none",
    color: "#4CAF50",
    cursor: "pointer",
    fontSize: "14px",
  },
};

export default LoginForm;
