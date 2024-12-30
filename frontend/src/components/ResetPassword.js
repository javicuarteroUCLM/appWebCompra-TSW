/** @format */

import React, { useState } from "react";
import userService from "../services/userService";
import { FaEnvelope } from "react-icons/fa";

const ResetPassword = () => {
  const [email, setEmail] = useState("");

  const sendResetPasswordEmail = async () => {
    try {
      console.log("Email: ", email);
      await userService.recoverPassword(email);
      console.log("Email sent");
    } catch (error) {
      alert("Error sending email");
    }
  };

  return (
    <div>
      {/* <Logo /> */}
      <h2>Reset your password.</h2>
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
      <button
        mode="contained"
        onClick={sendResetPasswordEmail}
        style={{ marginTop: 16 }}
      >
        Continue
      </button>
    </div>
  );
};

const styles = {
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#f5fcff",
  },
  title: {
    fontSize: 24,
    fontWeight: "bold",
    color: "#000",
  },
  form: {
    width: "80%",
    marginTop: 30,
  },
  inputGroup: {
    flexDirection: "row",
    alignItems: "center",
    borderBottomWidth: 1,
    borderBottomColor: "#cccccc",
    marginBottom: 10,
  },
  icon: {
    padding: 10,
    color: "#cccccc",
  },
  input: {
    flex: 1,
    height: 40,
  },
};

export default ResetPassword;
