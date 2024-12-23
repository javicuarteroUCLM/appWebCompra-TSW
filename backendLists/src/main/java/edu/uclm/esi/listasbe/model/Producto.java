package edu.uclm.esi.listasbe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Producto {
    @Id
    @Column(length = 36)
    private String id;
    @Column(length = 80, nullable = false)
    private String nombre;
    private float udsPedidas;
    private float udsCompradas;

    @ManyToOne
    private Lista lista;

    public Producto() {
        this.id = java.util.UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public float getUdsPedidas() {
        return udsPedidas;
    }

    public void setUdsPedidas(float udsPedidas) {
        this.udsPedidas = udsPedidas;
    }

    public float getUdsCompradas() {
        return udsCompradas;
    }

    public void setUdsCompradas(float udsCompradas) {
        this.udsCompradas = udsCompradas;
    }

    @JsonIgnore
    public Lista getLista() {
        return lista;
    }

    public void setLista(Lista lista) {
        this.lista = lista;
    }
}
