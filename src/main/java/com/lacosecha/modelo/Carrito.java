package com.lacosecha.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Carrito de compras guardado en la sesión HTTP.
 * Reglas: precios con IVA incluido; envío de Q25.00 si la compra es menor a Q200.00.
 */
public class Carrito implements Serializable {

    public static final BigDecimal TASA_IVA = new BigDecimal("1.12");
    public static final BigDecimal COSTO_ENVIO = new BigDecimal("25.00");
    public static final BigDecimal ENVIO_GRATIS_DESDE = new BigDecimal("200.00");

    private final Map<Integer, ItemCarrito> items = new LinkedHashMap<>();

    public void agregar(Producto p, int cantidad) {
        ItemCarrito item = items.get(p.getIdProducto());
        if (item == null) {
            items.put(p.getIdProducto(), new ItemCarrito(p, cantidad));
        } else {
            item.setProducto(p);  // refresca precio y stock
            item.setCantidad(item.getCantidad() + cantidad);
        }
    }

    public void actualizar(int idProducto, int cantidad) {
        if (cantidad <= 0) {
            items.remove(idProducto);
        } else if (items.containsKey(idProducto)) {
            items.get(idProducto).setCantidad(cantidad);
        }
    }

    public void eliminar(int idProducto) { items.remove(idProducto); }
    public void vaciar() { items.clear(); }
    public boolean isVacio() { return items.isEmpty(); }
    public List<ItemCarrito> getItems() { return new ArrayList<>(items.values()); }
    public int cantidadDe(int idProducto) {
        ItemCarrito i = items.get(idProducto);
        return i == null ? 0 : i.getCantidad();
    }

    public int getCantidadTotal() {
        int total = 0;
        for (ItemCarrito i : items.values()) total += i.getCantidad();
        return total;
    }

    /** Suma de productos (IVA incluido). */
    public BigDecimal getSubtotal() {
        BigDecimal s = BigDecimal.ZERO;
        for (ItemCarrito i : items.values()) s = s.add(i.getSubtotal());
        return s.setScale(2, RoundingMode.HALF_UP);
    }

    /** IVA contenido en el subtotal (desglose informativo). */
    public BigDecimal getIva() {
        BigDecimal sub = getSubtotal();
        return sub.subtract(sub.divide(TASA_IVA, 2, RoundingMode.HALF_UP));
    }

    public BigDecimal getCostoEnvio() {
        if (isVacio() || getSubtotal().compareTo(ENVIO_GRATIS_DESDE) >= 0) return BigDecimal.ZERO.setScale(2);
        return COSTO_ENVIO;
    }

    public BigDecimal getTotal() { return getSubtotal().add(getCostoEnvio()); }

    public BigDecimal getFaltaParaEnvioGratis() {
        BigDecimal falta = ENVIO_GRATIS_DESDE.subtract(getSubtotal());
        return falta.signum() > 0 ? falta : BigDecimal.ZERO;
    }
}
