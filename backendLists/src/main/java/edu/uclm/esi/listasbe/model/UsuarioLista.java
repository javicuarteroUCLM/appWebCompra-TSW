package edu.uclm.esi.listasbe.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios_listas")
public class UsuarioLista {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "usuario_id", nullable = false, length = 60)
    private String usuarioId; // Almacena únicamente el identificador del usuario (email o id)

    @ManyToOne
    @JoinColumn(name = "lista_id", nullable = false)
    private Lista lista;

    @Column(nullable = false)
    private boolean esPropietario;

    public UsuarioLista() {
    }

    public UsuarioLista(String usuarioId, Lista lista, boolean esPropietario) {
        this.usuarioId = usuarioId;
        this.lista = lista;
        this.esPropietario = esPropietario;
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

    public Lista getLista() {
        return lista;
    }

    public void setLista(Lista lista) {
        this.lista = lista;
    }

    public boolean isEsPropietario() {
        return esPropietario;
    }

    public void setEsPropietario(boolean esPropietario) {
        this.esPropietario = esPropietario;
    }

    @Override
    public String toString() {
        return "UsuarioLista{" +
                "id=" + id +
                ", usuarioId='" + usuarioId + '\'' +
                ", lista=" + lista +
                ", esPropietario=" + esPropietario +
                '}';
    }
}
