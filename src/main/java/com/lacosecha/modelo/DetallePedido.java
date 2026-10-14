package com.lacosecha.modelo;

import java.io.Serializable;
import java.math.BigDecimal;

/** Línea de un pedido: producto, cantidad y precio al momento de la compra. */
public class DetallePedido implements Serializable {

    private int idDetalle;
    private int idPedido;
    private int idProducto;
    private String productoNombre;
    private String productoCodigo;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public int getIdDetalle() { return idDetalle; }
    public void setIdDetalle(int idDetalle) { this.idDetalle = idDetalle; }
    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public String getProductoCodigo() { return productoCodigo; }
    public void setProductoCodigo(String productoCodigo) { this.productoCodigo = productoCodigo; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
