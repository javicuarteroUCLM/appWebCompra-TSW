import React, { useState, useEffect } from 'react';
import userService from '../services/userService';

const MyLists = () => {
  const [lists, setLists] = useState([]);
  const [newListName, setNewListName] = useState('');
  const [message, setMessage] = useState('');

  useEffect(() => {
    const fetchLists = async () => {
      try {
        const userLists = await userService.getLists();
        setLists(userLists);
      } catch (error) {
        setMessage('Error al cargar las listas.');
      }
    };

    fetchLists();
  }, []);

  const createList = async () => {
    if (!newListName.trim()) {
      setMessage('El nombre de la lista no puede estar vacío.');
      return;
    }

    try {
      const newList = await userService.createList(newListName.trim());
      setLists([...lists, newList]);
      setNewListName('');
      setMessage('Lista creada con éxito.');
    } catch (error) {
      setMessage('Error al crear la lista.');
    }
  };

  return (
    <div>
      <h1>Mis Listas</h1>
      <ul>
        {lists.map((list) => (
          <li key={list.id}>{list.nombre}</li>
        ))}
      </ul>
      <input
        type="text"
        placeholder="Nombre de la nueva lista"
        value={newListName}
        onChange={(e) => setNewListName(e.target.value)}
      />
      <button onClick={createList}>Crear Lista</button>
      {message && <p>{message}</p>}
    </div>
  );
};

export default MyLists;
