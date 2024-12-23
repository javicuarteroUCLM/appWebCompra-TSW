package edu.uclm.esi.listasbe.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Lista {
    @Id
    @Column(length = 36)
    private String id;
    @Column(length = 80)
    private String nombre;

    @OneToMany(mappedBy = "lista")
    private List<Producto> productos;

    @ElementCollection
    private List<String> emailsUsuarios;

    public Lista() {
        this.id = java.util.UUID.randomUUID().toString();
        this.productos = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public List<String> getEmailUsuarios() {
        return emailsUsuarios;
    }

    public void setEmailUsuarios(List<String> emailUsuarios) {
        this.emailsUsuarios = emailUsuarios;
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

    public void add(Producto producto) {
        this.productos.add(producto);
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

}
