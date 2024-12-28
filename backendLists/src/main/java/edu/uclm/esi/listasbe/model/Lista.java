package edu.uclm.esi.listasbe.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "listas")
public class Lista {
    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 80, nullable = false)
    private String nombre;

    @Column(nullable = false)
    private boolean compartida;

    @Column(length = 255, unique = true)
    private String urlInvitacion;

    @Column(nullable = false)
    private int maxUsuarios;

    @OneToMany(mappedBy = "lista", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Producto> productos;

    @OneToMany(mappedBy = "lista", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsuarioLista> usuarios;

    public Lista() {
        this.id = java.util.UUID.randomUUID().toString();
        this.productos = new ArrayList<>();
        this.usuarios = new ArrayList<>();
        this.compartida = false;
        this.maxUsuarios = 1; // Valor por defecto para usuarios no registrados
    }

    // Getters y setters

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
        producto.setLista(this); // Configura la relación bidireccional
        this.productos.add(producto);
    }

    public void removeProducto(Producto producto) {
        producto.setLista(null); // Rompe la relación bidireccional
        this.productos.remove(producto);
    }

    public List<UsuarioLista> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<UsuarioLista> usuarios) {
        this.usuarios = usuarios;
    }

    public void addUsuario(UsuarioLista usuarioLista) {
        usuarioLista.setLista(this); // Configura la relación bidireccional
        this.usuarios.add(usuarioLista);
    }

    public void removeUsuario(UsuarioLista usuarioLista) {
        usuarioLista.setLista(null); // Rompe la relación bidireccional
        this.usuarios.remove(usuarioLista);
    }
}
