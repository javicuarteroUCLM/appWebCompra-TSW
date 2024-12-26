package edu.uclm.esi.listasbe.dao;

import edu.uclm.esi.listasbe.model.RestriccionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RestriccionUsuarioRepository extends JpaRepository<RestriccionUsuario, Long> {

    // Buscar restricciones de un usuario específico
    RestriccionUsuario findByUsuarioId(String usuarioId);

    // Eliminar restricciones asociadas a un usuario específico
    void deleteByUsuarioId(String usuarioId);
}
