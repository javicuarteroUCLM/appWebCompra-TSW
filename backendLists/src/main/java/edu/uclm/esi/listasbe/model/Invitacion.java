package edu.uclm.esi.listasbe.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


@Entity
@Table(name = "invitaciones")
public class Invitacion {

    @Id
    @Column(length = 100)
    private String id;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Lista lista;

    @Column(nullable = false)
    private String emailInvitado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoInvitacion estado;

    public Invitacion() {
        this.id = java.util.UUID.randomUUID().toString();
    }

    public Invitacion(Lista lista, String emailInvitado, EstadoInvitacion estado) {
        this.id = java.util.UUID.randomUUID().toString();
        this.lista = lista;
        this.emailInvitado = emailInvitado;
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonIgnore
    public Lista getLista() {
        return lista;
    }

    public void setLista(Lista lista) {
        this.lista = lista;
    }

    public String getEmailInvitado() {
        return emailInvitado;
    }

    public void setEmailInvitado(String emailInvitado) {
        this.emailInvitado = emailInvitado;
    }

    public EstadoInvitacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoInvitacion estado) {
        this.estado = estado;
    }

    public enum EstadoInvitacion {
        PENDIENTE,
        ACEPTADA,
        RECHAZADA
    }
}
