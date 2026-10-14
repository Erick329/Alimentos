package com.lacosecha.modelo;

import java.math.BigDecimal;

/** Fila genérica para reportes: una etiqueta, un monto y una cantidad. */
public class FilaReporte {

    private String etiqueta;
    private String detalle;
    private BigDecimal monto = BigDecimal.ZERO;
    private long cantidad;

    public FilaReporte() { }

    public FilaReporte(String etiqueta, BigDecimal monto, long cantidad) {
        this.etiqueta = etiqueta;
        this.monto = monto == null ? BigDecimal.ZERO : monto;
        this.cantidad = cantidad;
    }

    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public long getCantidad() { return cantidad; }
    public void setCantidad(long cantidad) { this.cantidad = cantidad; }
}
