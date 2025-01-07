package edu.uclm.esi.listasbe.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import edu.uclm.esi.listasbe.model.Invitacion;



@Repository
public interface InvitacionDao extends CrudRepository<Invitacion, String> {

    List<Invitacion> findByEmailInvitado(String email);
    Optional<Invitacion> findByEmailInvitadoAndLista_IdAndEstado(String email, String listaId, Invitacion.EstadoInvitacion estado);

}
