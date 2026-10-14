package com.lacosecha.modelo;

import java.io.Serializable;
import java.util.Date;

/** Tabla Usuarios: clientes, vendedores y administradores. */
public class Usuario implements Serializable {

    public static final int MAX_INTENTOS = 5;

    private int idUsuario;
    private String nombre;
    private String apellido;
    private String email;
    private String nit = "CF";
    private String password;          // hash BCrypt
    private String telefono;
    private String direccion;
    private String status = "ACTIVO"; // ACTIVO / INACTIVO
    private Date fechaRegistro;
    private int intentosFallidos;
    private int idRol;
    private String rol;               // nombre del rol (ADMIN, VENDEDOR, CLIENTE)
    private int totalPedidos;         // solo para el listado de clientes

    public String getNombreCompleto() { return nombre + " " + apellido; }
    public boolean isActivo() { return "ACTIVO".equals(status); }
    public boolean isBloqueado() { return intentosFallidos >= MAX_INTENTOS; }
    public boolean isAdmin() { return "ADMIN".equals(rol); }
    public boolean isVendedor() { return "VENDEDOR".equals(rol); }
    public boolean isPersonal() { return isAdmin() || isVendedor(); }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }
    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public int getTotalPedidos() { return totalPedidos; }
    public void setTotalPedidos(int totalPedidos) { this.totalPedidos = totalPedidos; }
}
