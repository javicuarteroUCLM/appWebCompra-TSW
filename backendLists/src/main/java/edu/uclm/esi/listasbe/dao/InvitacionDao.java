package edu.uclm.esi.listasbe.dao;

import edu.uclm.esi.listasbe.model.Invitacion;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitacionDao extends CrudRepository<Invitacion, String> {

    List<Invitacion> findByEmailInvitado(String email);

}
