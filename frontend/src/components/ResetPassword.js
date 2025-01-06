/** @format */

import React, { useState } from "react";
import userService from "../services/userService";
import { FaEnvelope } from "react-icons/fa";

const ResetPassword = () => {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");

  const sendResetPasswordEmail = async () => {
    try {
      console.log("Email: ", email);
      await userService.recoverPassword(email);
      setMessage("¡Correo de recuperación enviado!");
    } catch (error) {
      alert("Error sending email");
      setMessage("Error al enviar el correo.");
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.formContainer}>
        <h2 style={styles.title}>Restablecer Contraseña</h2>
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
        <button onClick={sendResetPasswordEmail} style={styles.button}>
          Continuar
        </button>
        {message && <p style={styles.message}>{message}</p>}
      </div>
    </div>
  );
};

const styles = {
  container: {
    display: "flex",
    justifyContent: "center", // Centra el contenido horizontalmente
    alignItems: "center", // Centra el contenido verticalmente
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
    color: "#000",
    marginBottom: "20px",
  },
  inputGroup: {
    display: "flex",
    alignItems: "center",
    backgroundColor: "#f0f0f0",
    borderRadius: "5px",
    padding: "10px",
    marginBottom: "15px",
  },
  icon: {
    marginRight: "10px",
    color: "#4CAF50",
  },
  input: {
    flex: 1, // Ajusta el tamaño del input automáticamente
    border: "none",
    outline: "none",
    backgroundColor: "transparent",
    fontSize: "16px",
    color: "#333",
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
    width: "100%",
  },
  message: {
    marginTop: "15px",
    fontSize: "14px",
    color: "#ff5722",
  },
};

export default ResetPassword;
