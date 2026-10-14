package com.lacosecha.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Tabla Productos. El precio incluye IVA (12 %).
 * El stock es la suma de las cantidades de sus lotes ACTIVOS (tabla Lotes).
 */
public class Producto implements Serializable {

    private int idProducto;
    private String nombre;
    private String descripcion;
    private String codigo;
    private BigDecimal precio = BigDecimal.ZERO;
    private int stock;
    private int stockMinimo;
    private String status = "ACTIVO";
    private int idCategoria;
    private String categoriaNombre;
    private Date proximoVencimiento;   // fecha del lote con existencias que vence primero (calculada)

    public boolean isActivo() { return "ACTIVO".equals(status); }
    /** Ícono para mostrar en la web según la categoría (la tabla no guarda imágenes). */
    public String getIcono() {
        String c = categoriaNombre == null ? "" : categoriaNombre.toLowerCase();
        if (c.contains("grano")) return "🌾";
        if (c.contains("lácteo") || c.contains("lacteo")) return "🥛";
        if (c.contains("bebida")) return "🧃";
        if (c.contains("snack")) return "🍪";
        if (c.contains("fruta") || c.contains("verdura")) return "🥑";
        return "🛒";
    }

    public boolean isBajoStock() { return stock <= stockMinimo; }
    public boolean isAgotado() { return stock <= 0; }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }
    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }
    public Date getProximoVencimiento() { return proximoVencimiento; }
    public void setProximoVencimiento(Date proximoVencimiento) { this.proximoVencimiento = proximoVencimiento; }
}
