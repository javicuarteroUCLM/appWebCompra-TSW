package edu.uclm.esi.listasbe.dao;

import org.springframework.data.repository.CrudRepository;

import edu.uclm.esi.listasbe.model.Producto;

public interface ProductoDao extends CrudRepository<Producto, String> {

}
