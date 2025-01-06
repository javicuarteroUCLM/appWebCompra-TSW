/** @format */

import React, { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import userService from "../services/userService";

const ConfirmAccount = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const queryParams = new URLSearchParams(location.search);
  const token = queryParams.get("token");

  useEffect(() => {
    const confirmAccount = async () => {
      try {
        const response = await userService.confirmAccount(token);
        console.log("Respuesta del servidor:", response);
        if (response === 200) {
          setTimeout(() => {
            navigate("/login");
          }, 1500);
        } else {
          navigate("/confirmarCuenta");
          console.log("Ocurrió un error al confirmar la cuenta.");
        }
      } catch (error) {
        navigate("/confirmarCuenta");
        console.error("Error al confirmar la cuenta:", error);
      }
    };

    if (token) {
      confirmAccount();
    } else {
      console.log("No se proporcionó un token.");
    }
  }, [token, navigate]);

  return (
    <div style={styles.container}>
      {token ? (
        <div>
          <p style={styles.textoFeed}>
            ¡Gracias por registrarse en nuestra aplicación! Redirigiendo al login...
          </p>
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="100"
            height="100"
            fill="green"
            className="bi bi-check"
            viewBox="0 0 16 16"
          >
            <path d="M13.485 1.929a.5.5 0 0 1 .707.707l-8 8a.5.5 0 0 1-.708 0l-4-4a.5.5 0 1 1 .707-.707L6 9.293l7.485-7.364z" />
          </svg>
        </div>
      ) : (
        <div>
          <p style={styles.textoFeed}>Token inválido o expirado.</p>
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="100"
            height="100"
            fill="red"
            className="bi bi-x-circle"
            viewBox="0 0 16 16"
          >
            <path d="M8 15A7 7 0 1 0 8 1a7 7 0 0 0 0 14zm0-1A6 6 0 1 1 8 2a6 6 0 0 1 0 12z" />
            <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z" />
          </svg>
        </div>
      )}
    </div>
  );
};

const styles = {
  container: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    height: "100vh",
    textAlign: "center",
    flexDirection: "column",
    backgroundColor: "#f0f0f0",
    padding: "20px",
    borderRadius: "10px",
    boxShadow: "0 4px 8px rgba(0, 0, 0, 0.1)",
  },
  textoFeed: {
    fontSize: "2rem",
    marginBottom: "1.5rem",
    color: "#333",
    fontWeight: "bold",
  },
};

export default ConfirmAccount;
