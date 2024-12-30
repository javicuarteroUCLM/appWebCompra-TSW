package edu.uclm.esi.listasbe.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import edu.uclm.esi.listasbe.model.Lista;

public interface ListaDao extends CrudRepository<Lista, String> {
    @Query(value = "SELECT lista_id FROM usuarios_listas WHERE usuario_id = :email", nativeQuery = true)
    List<String> getListasDe(@Param("email") String email);

}
