package edu.uclm.esi.listasbe.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "restricciones_usuarios")
public class RestriccionUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "usuario_id", nullable = false, length = 60)
    private String usuarioId; // Identificador del usuario (email o ID)

    @Column(nullable = false)
    private int numListasActuales; // Número de listas creadas actualmente

    @Column(nullable = false)
    private int numProductosPorLista; // Límite de productos por lista

    public RestriccionUsuario() {
    }

    public RestriccionUsuario(String usuarioId, int numListasActuales, int numProductosPorLista) {
        this.usuarioId = usuarioId;
        this.numListasActuales = numListasActuales;
        this.numProductosPorLista = numProductosPorLista;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public int getNumListasActuales() {
        return numListasActuales;
    }

    public void setNumListasActuales(int numListasActuales) {
        this.numListasActuales = numListasActuales;
    }

    public int getNumProductosPorLista() {
        return numProductosPorLista;
    }

    public void setNumProductosPorLista(int numProductosPorLista) {
        this.numProductosPorLista = numProductosPorLista;
    }

    @Override
    public String toString() {
        return "RestriccionUsuario{" +
                "id=" + id +
                ", usuarioId='" + usuarioId + '\'' +
                ", numListasActuales=" + numListasActuales +
                ", numProductosPorLista=" + numProductosPorLista +
                '}';
    }
}
