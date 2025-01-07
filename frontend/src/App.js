/** @format */

import React from "react";
import { Elements } from "@stripe/react-stripe-js";
import { loadStripe } from "@stripe/stripe-js";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import LandingPage from "./components/LandingPage";
import RegisterForm from "./components/RegisterForm";
import LoginForm from "./components/LoginForm";
import UserDashboard from "./components/UserDashboard";
import ResetPassword from "./components/ResetPassword";
import ConfirmAccount from "./components/ConfirmAccount";
import InvitationHandler from "./components/InvitationHandler";

const fetchStripeKey = async () => {
  const response = await fetch("http://localhost:9000/pagos/stripeKey");
  const stripeKey = await response.text();
  return loadStripe(stripeKey);
};

const stripePromise = fetchStripeKey();

const App = () => {
  return (
    <Router>
      <Elements stripe={stripePromise}>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/register" element={<RegisterForm />} />
          <Route path="/login" element={<LoginForm />} />
          <Route path="/dashboard" element={<UserDashboard />} />
          <Route path="/reset-password" element={<ResetPassword />} />
          <Route path="/confirmarCuenta" element={<ConfirmAccount />} />
          <Route path="/invitacion/:idLista" element={<InvitationHandler />} />
        </Routes>
      </Elements>
    </Router>
  );
};

export default App;
