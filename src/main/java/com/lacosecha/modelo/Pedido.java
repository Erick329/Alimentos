package com.lacosecha.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Tabla Pedidos (encabezado de la venta) con su detalle y los datos del último pago. */
public class Pedido implements Serializable {

    /** Estados posibles, en el orden normal del flujo. */
    public static final String[] ESTADOS = {
        "PENDIENTE", "PAGADO", "EN_PREPARACION", "ENVIADO", "ENTREGADO", "CANCELADO"
    };

    private int idPedido;
    private int idUsuario;
    private String clienteNombre;
    private String clienteEmail;
    private String clienteNit;
    private Date fechaPedido;
    private String estado;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal costoEnvio;
    private BigDecimal total;
    private String direccionEnvio;
    private String metodoPago;
    private String estadoPago;
    private String referenciaPago;
    private int totalArticulos;
    private List<DetallePedido> detalles = new ArrayList<>();

    /** Clase CSS de Bootstrap para pintar el estado. */
    public String getColorEstado() {
        if (estado == null) return "secondary";
        switch (estado) {
            case "PENDIENTE": return "warning";
            case "PAGADO": return "info";
            case "EN_PREPARACION": return "primary";
            case "ENVIADO": return "dark";
            case "ENTREGADO": return "success";
            case "CANCELADO": return "danger";
            default: return "secondary";
        }
    }

    public String getEstadoTexto() { return estado == null ? "" : estado.replace('_', ' '); }
    public String[] getEstados() { return ESTADOS; }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }
    public String getClienteNit() { return clienteNit; }
    public void setClienteNit(String clienteNit) { this.clienteNit = clienteNit; }
    public Date getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(Date fechaPedido) { this.fechaPedido = fechaPedido; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getIva() { return iva; }
    public void setIva(BigDecimal iva) { this.iva = iva; }
    public BigDecimal getCostoEnvio() { return costoEnvio; }
    public void setCostoEnvio(BigDecimal costoEnvio) { this.costoEnvio = costoEnvio; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getDireccionEnvio() { return direccionEnvio; }
    public void setDireccionEnvio(String direccionEnvio) { this.direccionEnvio = direccionEnvio; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }
    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }
    public int getTotalArticulos() { return totalArticulos; }
    public void setTotalArticulos(int totalArticulos) { this.totalArticulos = totalArticulos; }
    public List<DetallePedido> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedido> detalles) { this.detalles = detalles; }
}
