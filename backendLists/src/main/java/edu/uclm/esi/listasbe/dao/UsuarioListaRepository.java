package edu.uclm.esi.listasbe.dao;

import edu.uclm.esi.listasbe.model.UsuarioLista;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface UsuarioListaRepository extends JpaRepository<UsuarioLista, Long> {

    // Buscar listas asociadas a un usuario por su ID (email)
    List<UsuarioLista> findByUsuarioId(String usuarioId);

    // Buscar todos los usuarios asociados a una lista específica
    List<UsuarioLista> findByListaId(String listaId);

    // Buscar el propietario de una lista específica
    UsuarioLista findByListaIdAndEsPropietarioTrue(String listaId);
}
