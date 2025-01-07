import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";

const InvitationHandler = () => {
  const { idLista } = useParams();
  const [lista, setLista] = useState(null);
  const [idInvitacion, setIdInvitacion] = useState(null);
  const [error, setError] = useState(null);

  const navigate = useNavigate();

  useEffect(() => {
    const fetchInvitation = async () => {
      try {
        const token = localStorage.getItem("authToken");
        if (!token) {
          setError("Inicia sesión primero o regístrate para poder aceptar la invitación.");
          return;
        }

        const response = await axios.get(
          `http://localhost:8383/listas/invitacion/${idLista}`,
          {
            headers: { token },
          }
        );

        setLista(response.data);
        setIdInvitacion(response.data.idInvitacion);
      } catch (err) {
        console.error("Error al cargar la invitación", err);
        setError("No se pudo cargar la invitación.");
      }
    };

    fetchInvitation();
  }, [idLista]);

  const handleResponse = async (aceptar) => {
    try {
      const token = localStorage.getItem("authToken");
      if (!token) {
        setError("Inicia sesión primero para poder aceptar la invitación.");
        return;
      }
      const status = aceptar ? "aceptado" : "rechazado";

      await axios.post(
        `http://localhost:8383/listas/aceptarInvitacion`,
        JSON.stringify(status),
        {
          headers: {
            "Content-Type": "application/json",
            idInvitacion,
            token,
          },
        }
      );
      alert(aceptar ? "¡Invitación aceptada!" : "Invitación rechazada.");
      navigate("/dashboard");
    } catch (err) {
      console.error("Error procesando la invitación:", err);
      setError("No se pudo procesar la invitación.");
    }
  };

  if (error) {
    return (
      <div style={styles.container}>
        <div style={styles.loginCard}>
          <h1 style={styles.title}>¡Aún no eres de los nuestros!</h1>
          <p style={styles.text}>{error}</p>
          {!localStorage.getItem("authToken") && (
            <button style={styles.loginButton} onClick={() => navigate("/login")}>
              Iniciar Sesión
            </button>
          )}
        </div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      {lista ? (
        <div style={styles.card}>
          <h1 style={styles.title}>Invitación a la lista</h1>
          <p style={styles.text}>
            Te han invitado a la lista: <strong>{lista.nombreLista}</strong>
          </p>
          <div style={styles.buttonContainer}>
            <button
              style={{ ...styles.button, ...styles.acceptButton }}
              onClick={() => handleResponse(true)}
            >
              Aceptar
            </button>
            <button
              style={{ ...styles.button, ...styles.rejectButton }}
              onClick={() => handleResponse(false)}
            >
              Rechazar
            </button>
          </div>
        </div>
      ) : (
        <p style={styles.loading}>Cargando invitación...</p>
      )}
    </div>
  );
};

const styles = {
  container: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    justifyContent: "center",
    minHeight: "100vh",
    padding: "20px",
    backgroundColor: "#f9f9f9",
    fontFamily: "'Roboto', sans-serif",
    textAlign: "center",
  },
  card: {
    backgroundColor: "#fff",
    padding: "20px",
    borderRadius: "10px",
    boxShadow: "0 4px 8px rgba(0, 0, 0, 0.1)",
    maxWidth: "400px",
    width: "100%",
  },
  loginCard: {
    backgroundColor: "#fff",
    padding: "30px",
    borderRadius: "10px",
    boxShadow: "0 6px 10px rgba(0, 0, 0, 0.15)",
    textAlign: "center",
    maxWidth: "400px",
    width: "100%",
  },
  title: {
    fontSize: "24px",
    color: "#333",
    marginBottom: "10px",
  },
  text: {
    fontSize: "16px",
    color: "#555",
    marginBottom: "20px",
  },
  buttonContainer: {
    display: "flex",
    justifyContent: "space-between",
    gap: "10px",
  },
  button: {
    padding: "10px 15px",
    borderRadius: "5px",
    border: "none",
    cursor: "pointer",
    fontSize: "16px",
    flex: 1,
    transition: "background-color 0.3s",
  },
  acceptButton: {
    backgroundColor: "#4CAF50",
    color: "#fff",
  },
  rejectButton: {
    backgroundColor: "#f44336",
    color: "#fff",
  },
  loginButton: {
    backgroundColor: "#007BFF",
    color: "#fff",
    border: "none",
    borderRadius: "5px",
    padding: "10px 20px",
    fontSize: "16px",
    cursor: "pointer",
    transition: "background-color 0.3s",
  },
  loginButtonHover: {
    backgroundColor: "#0056b3",
  },
  error: {
    color: "#f44336",
    fontSize: "18px",
    marginBottom: "20px",
  },
  loading: {
    fontSize: "16px",
    color: "#777",
  },
};

export default InvitationHandler;
