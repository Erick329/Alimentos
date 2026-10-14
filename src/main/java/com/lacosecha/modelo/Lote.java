package com.lacosecha.modelo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/** Tabla Lotes: existencias de un producto con su fecha de vencimiento. */
public class Lote {

    private int idLote;
    private String numeroLote;
    private int cantidad;
    private Date fechaVencimiento;
    private Date fechaIngreso;
    private String status = "ACTIVO";
    private int idProducto;
    private String productoNombre;
    private String productoCodigo;

    /** Días que faltan para vencer (negativo si ya venció). */
    public long getDiasParaVencer() {
        if (fechaVencimiento == null) return Long.MAX_VALUE;
        LocalDate v = fechaVencimiento instanceof java.sql.Date
                ? ((java.sql.Date) fechaVencimiento).toLocalDate()
                : fechaVencimiento.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return ChronoUnit.DAYS.between(LocalDate.now(), v);
    }

    public boolean isVencido() { return getDiasParaVencer() < 0; }

    /** Texto para mostrar en tablas: "Vencido", "Vence hoy", "Vence en 5 días"... */
    public String getEstadoVencimiento() {
        long d = getDiasParaVencer();
        if (d < 0) return "VENCIDO";
        if (d == 0) return "Vence hoy";
        if (d <= 30) return "Vence en " + d + " día(s)";
        return "Vigente";
    }

    public int getIdLote() { return idLote; }
    public void setIdLote(int idLote) { this.idLote = idLote; }
    public String getNumeroLote() { return numeroLote; }
    public void setNumeroLote(String numeroLote) { this.numeroLote = numeroLote; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public Date getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(Date fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public Date getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(Date fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public String getProductoCodigo() { return productoCodigo; }
    public void setProductoCodigo(String productoCodigo) { this.productoCodigo = productoCodigo; }
}
