package edu.uclm.esi.listasbe.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import edu.uclm.esi.listasbe.model.Lista;

public interface ListaDao extends CrudRepository<Lista, String> {
    @Query(value = "select lista_id from lista_emails_usuarios where emails_usuarios = :email", nativeQuery = true)
    List<String> getListasDe(@Param("email") String email);

}
