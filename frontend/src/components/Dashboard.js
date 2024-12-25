import React, { useState, useEffect } from 'react';
import userService from '../services/userService';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
  const [lists, setLists] = useState([]);
  const [isPremium, setIsPremium] = useState(false);
  const [newListName, setNewListName] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  // Cargar datos del usuario al iniciar
  useEffect(() => {
    const fetchData = async () => {
      try {
        const userData = await userService.getUserDetails(); // Llama al backend para obtener los detalles del usuario
        setIsPremium(userData.isPremium); // Lo de isPremium me lo he inventado, pero debería ser algo que devuelva el backend comprobando que ha pagado
        setLists(userData.lists || []);
      } catch (error) {
        console.error('Error al cargar los datos del usuario:', error);
      }
    };
    const fetchLists = async () => {
        try {
          const userLists = await userService.getLists();
          setLists(userLists);
        } catch (error) {
          console.error('Error al cargar las listas:', error);
        }
      };
  
      const fetchUserStatus = async () => {
        try {
          const userData = await userService.getUserDetails();
          setIsPremium(userData.isPremium);
        } catch (error) {
          console.error('Error al cargar el estado del usuario:', error);
        }
      };

    fetchData();
    fetchLists();
    fetchUserStatus();
  }, []);

  // Crear una nueva lista
  const createList = async () => {
    if (!newListName.trim()) {
        setMessage('El nombre de la lista no puede estar vacío.');
        return;
    }

    if (!isPremium && lists.length >= 2) {
      setMessage('Los usuarios gratuitos solo pueden crear 2 listas.');
      return;
    }

    try {
        const newList = await userService.createList(newListName);
        setLists([...lists, newList]);
        setNewListName(''); 
        setMessage('Lista creada con éxito.');
      } catch (error) {
        setMessage('Error al crear la lista.');
      }
  };

  // Convertirse en usuario premium
  const becomePremium = () => {
    navigate('/pay'); // Redirige al formulario de pago
  };

  return (
    <div style={{ textAlign: 'center', marginTop: '20px' }}>
      <h1>Bienvenido al Dashboard</h1>
      <p>Estado de usuario: {isPremium ? 'Premium' : 'Gratuito'}</p>

      <div style={{ marginBottom: '20px' }}>
        <input
          type="text"
          placeholder="Nombre de la lista"
          value={newListName}
          onChange={(e) => setNewListName(e.target.value)}
          style={{ padding: '10px', marginRight: '10px' }}
        />
        <button onClick={createList} style={{ padding: '10px 20px' }}>
          Crear Lista
        </button>
      </div>

      <button onClick={becomePremium} style={{ padding: '10px 20px', marginBottom: '20px' }}>
        Hacerse Premium
      </button>

      <h2>Tus Listas</h2>
      <ul>
        {lists.map((list) => (
          <li key={list.id}>{list.nombre}</li>
        ))}
      </ul>

      {message && <p style={{ color: 'red' }}>{message}</p>}
    </div>
  );
};

export default Dashboard;
