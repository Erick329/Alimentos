package com.lacosecha.modelo;

import java.io.Serializable;
import java.math.BigDecimal;

/** Producto dentro del carrito con la cantidad elegida. */
public class ItemCarrito implements Serializable {

    private Producto producto;
    private int cantidad;

    public ItemCarrito(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public BigDecimal getSubtotal() {
        return producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
    }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
