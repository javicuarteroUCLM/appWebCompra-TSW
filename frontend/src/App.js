import React from 'react';
import { Elements } from '@stripe/react-stripe-js';
import { loadStripe } from '@stripe/stripe-js';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import LandingPage from './components/LandingPage';
import RegisterForm from './components/RegisterForm';
import LoginForm from './components/LoginForm';
import UserDashboard from './components/UserDashboard';

const stripePromise = loadStripe('pk_test_51Q7a5l1FZhFh3pfsdQHD46vushsIqyxsKtiWJQm85vYXs0uBFu3ZCl7BOoP3o54b0eswWFR3ApITMbPPOSN8d66a00Ecx2pqtn'); // Usamos la variable de entorno para la clave pública

const App = () => {
  return (
    <Router>
      <Elements stripe={stripePromise}>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/register" element={<RegisterForm />} />
          <Route path="/login" element={<LoginForm />} />
          <Route path="/dashboard" element={<UserDashboard />} />
        </Routes>
      </Elements>
    </Router>
  );
};

export default App;
