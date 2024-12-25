import React, { useState } from 'react';
import userService from '../services/userService';

const AddProduct = ({ listId }) => {
    const [productName, setProductName] = useState('');
    const [units, setUnits] = useState('');
    const [message, setMessage] = useState('');
  
    const addProduct = async () => {
      try {
        const product = { nombre: productName, udsPedidas: units };
        await userService.addProduct(listId, product);
        setMessage('Producto agregado con éxito.');
      } catch (error) {
        setMessage('Error al agregar el producto.');
      }
    };
  
    return (
      <div>
        <h2>Agregar Producto</h2>
        <input
          type="text"
          placeholder="Nombre del producto"
          value={productName}
          onChange={(e) => setProductName(e.target.value)}
        />
        <input
          type="number"
          placeholder="Unidades"
          value={units}
          onChange={(e) => setUnits(e.target.value)}
        />
        <button onClick={addProduct}>Agregar</button>
        {message && <p>{message}</p>}
      </div>
    );
  };
  
  export default AddProduct;
  