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

    @Column(nullable = false)
    private boolean compartida;

    @Column(length = 255)
    private String urlInvitacion;

    @Column(nullable = false)
    private int maxUsuarios;

    @OneToMany(mappedBy = "lista")
    private List<Producto> productos;

    @ElementCollection
    private List<String> emailsUsuarios;

    public Lista() {
        this.id = java.util.UUID.randomUUID().toString();
        this.productos = new ArrayList<>();
        this.compartida = false;
        this.maxUsuarios = 1; // Por defecto para usuarios no registrados
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

    public boolean isCompartida() {
        return compartida;
    }

    public void setCompartida(boolean compartida) {
        this.compartida = compartida;
    }

    public String getUrlInvitacion() {
        return urlInvitacion;
    }

    public void setUrlInvitacion(String urlInvitacion) {
        this.urlInvitacion = urlInvitacion;
    }

    public int getMaxUsuarios() {
        return maxUsuarios;
    }

    public void setMaxUsuarios(int maxUsuarios) {
        this.maxUsuarios = maxUsuarios;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    public void addProducto(Producto producto) {
        this.productos.add(producto);
    }

    public List<String> getEmailsUsuarios() {
        return emailsUsuarios;
    }

    public void setEmailsUsuarios(List<String> emailsUsuarios) {
        this.emailsUsuarios = emailsUsuarios;
    }
}
