import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import LandingPage from './components/LandingPage';
import RegisterForm from './components/RegisterForm';
import LoginForm from './components/LoginForm';
import PaymentForm from './components/PaymentForm';
import Dashboard from './components/Dashboard';
import MyLists from './components/MyLists';
import AddProduct from './components/AddProduct';

const App = () => {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/register" element={<RegisterForm />} />
        <Route path="/login" element={<LoginForm />} />
        <Route path="/pay" element={<PaymentForm />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/my-lists" element={<MyLists />} />
        <Route path="/add-product" element={<AddProduct />} />
      </Routes>
    </Router>
  );
};

export default App;
