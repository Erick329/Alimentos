package com.lacosecha.servicio;

import com.lacosecha.dao.ConexionBD;
import com.lacosecha.dao.LoteDAO;
import com.lacosecha.dao.PedidoDAO;
import com.lacosecha.dao.ProductoDAO;
import com.lacosecha.modelo.Carrito;
import com.lacosecha.modelo.DetallePedido;
import com.lacosecha.modelo.ItemCarrito;
import com.lacosecha.modelo.Lote;
import com.lacosecha.modelo.MovimientoInventario;
import com.lacosecha.modelo.Pedido;
import com.lacosecha.modelo.Producto;
import com.lacosecha.modelo.Usuario;
import com.lacosecha.util.NegocioException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Lógica del PROCESO PRINCIPAL (la venta) y del inventario por lotes.
 * Cada operación se ejecuta dentro de UNA transacción: si algo falla (stock, pago, etc.)
 * se hace ROLLBACK y la base de datos queda exactamente como estaba.
 */
public class PedidoService {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final LoteDAO loteDAO = new LoteDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final PagoService pagoService = new PagoService();

    /**
     * Registra una venta.
     * @param cliente           cliente al que se le vende
     * @param idUsuarioRegistra quien registra la venta (el mismo cliente o el vendedor)
     */
    public Pedido crearPedido(Usuario cliente, Carrito carrito, String direccion, String metodoPago,
                              String tarjeta, String vencimiento, String cvv, int idUsuarioRegistra)
            throws NegocioException, SQLException {

        if (carrito == null || carrito.isVacio()) throw new NegocioException("El carrito está vacío.");

        try (Connection cn = ConexionBD.getConexion()) {
            cn.setAutoCommit(false);                                          // BEGIN TRANSACTION
            try {
                Pedido pedido = new Pedido();
                pedido.setIdUsuario(cliente.getIdUsuario());
                pedido.setDireccionEnvio(direccion);
                BigDecimal subtotal = BigDecimal.ZERO;
                List<List<Lote>> lotesPorItem = new ArrayList<>();

                // 1. Validar existencias en lotes NO vencidos (filas bloqueadas con FOR UPDATE)
                for (ItemCarrito item : carrito.getItems()) {
                    Producto p = productoDAO.bloquear(cn, item.getProducto().getIdProducto());
                    if (p == null || !p.isActivo()) {
                        throw new NegocioException("El producto \"" + item.getProducto().getNombre() + "\" ya no está disponible.");
                    }
                    List<Lote> lotes = loteDAO.disponiblesParaVenta(cn, p.getIdProducto());
                    int disponible = 0;
                    for (Lote l : lotes) disponible += l.getCantidad();
                    if (disponible < item.getCantidad()) {
                        throw new NegocioException("Existencias insuficientes de \"" + p.getNombre()
                                + "\". Disponibles (sin contar lotes vencidos): " + disponible + ".");
                    }
                    lotesPorItem.add(lotes);
                    DetallePedido d = new DetallePedido();
                    d.setIdProducto(p.getIdProducto());
                    d.setProductoNombre(p.getNombre());
                    d.setCantidad(item.getCantidad());
                    d.setPrecioUnitario(p.getPrecio());                       // precio vigente en la BD
                    d.setSubtotal(p.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())));
                    pedido.getDetalles().add(d);
                    subtotal = subtotal.add(d.getSubtotal());
                }

                // 2. Totales (precios con IVA incluido; envío gratis desde Q200)
                subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
                BigDecimal envio = subtotal.compareTo(Carrito.ENVIO_GRATIS_DESDE) >= 0 ? BigDecimal.ZERO : Carrito.COSTO_ENVIO;
                BigDecimal iva = subtotal.subtract(subtotal.divide(Carrito.TASA_IVA, 2, RoundingMode.HALF_UP));
                pedido.setSubtotal(subtotal);
                pedido.setIva(iva);
                pedido.setCostoEnvio(envio);
                pedido.setTotal(subtotal.add(envio));

                // 3. Pago (pasarela simulada)
                PagoService.ResultadoPago pago = pagoService.procesar(metodoPago, pedido.getTotal(), tarjeta, vencimiento, cvv);
                if (!pago.isAprobado()) throw new NegocioException(pago.getMensaje());
                pedido.setEstado("APROBADO".equals(pago.getEstado()) ? "PAGADO" : "PENDIENTE");

                // 4. Pedido, detalle y pago
                int idPedido = pedidoDAO.insertar(cn, pedido);
                pedido.setIdPedido(idPedido);
                pedidoDAO.insertarPago(cn, idPedido, metodoPago, pedido.getTotal(), pago.getEstado(), pago.getReferencia());

                // 5. Descontar de los lotes (FEFO) y registrar cada salida en Movimientos
                for (int i = 0; i < pedido.getDetalles().size(); i++) {
                    DetallePedido d = pedido.getDetalles().get(i);
                    int pendiente = d.getCantidad();
                    for (Lote l : lotesPorItem.get(i)) {
                        if (pendiente == 0) break;
                        int tomar = Math.min(pendiente, l.getCantidad());
                        loteDAO.sumarCantidad(cn, l.getIdLote(), -tomar);
                        productoDAO.registrarMovimiento(cn, "SALIDA", tomar, "Venta pedido #" + idPedido,
                                d.getIdProducto(), idUsuarioRegistra, l.getIdLote());
                        pendiente -= tomar;
                    }
                    productoDAO.recalcularStock(cn, d.getIdProducto());
                }

                cn.commit();                                                  // COMMIT: todo o nada
                pedido.setMetodoPago(metodoPago);
                pedido.setEstadoPago(pago.getEstado());
                pedido.setReferenciaPago(pago.getReferencia());
                return pedido;
            } catch (NegocioException | SQLException e) {
                cn.rollback();                                                // ROLLBACK
                throw e;
            }
        }
    }

    /** Transiciones permitidas entre estados del pedido. */
    public static List<String> siguientesEstados(String actual) {
        switch (actual) {
            case "PENDIENTE": return Arrays.asList("PAGADO", "CANCELADO");
            case "PAGADO": return Arrays.asList("EN_PREPARACION", "CANCELADO");
            case "EN_PREPARACION": return Arrays.asList("ENVIADO", "CANCELADO");
            case "ENVIADO": return Arrays.asList("ENTREGADO");
            default: return Arrays.asList();
        }
    }

    /**
     * Cambia el estado de un pedido. Si se cancela, las unidades regresan
     * a los MISMOS lotes de los que salieron.
     */
    public void cambiarEstado(int idPedido, String nuevoEstado, int idUsuario) throws NegocioException, SQLException {
        Pedido pedido = pedidoDAO.buscar(idPedido);
        if (pedido == null) throw new NegocioException("El pedido no existe.");
        if (!siguientesEstados(pedido.getEstado()).contains(nuevoEstado)) {
            throw new NegocioException("No se puede pasar de " + pedido.getEstadoTexto() + " a " + nuevoEstado.replace('_', ' ') + ".");
        }
        try (Connection cn = ConexionBD.getConexion()) {
            cn.setAutoCommit(false);
            try {
                pedidoDAO.actualizarEstado(cn, idPedido, nuevoEstado);
                if ("CANCELADO".equals(nuevoEstado)) {
                    for (MovimientoInventario m : productoDAO.salidasDeVenta(cn, idPedido)) {
                        if (m.getIdLote() != null) loteDAO.sumarCantidad(cn, m.getIdLote(), m.getCantidad());
                        productoDAO.registrarMovimiento(cn, "ENTRADA", m.getCantidad(), "Cancelación pedido #" + idPedido,
                                m.getIdProducto(), idUsuario, m.getIdLote());
                        productoDAO.recalcularStock(cn, m.getIdProducto());
                    }
                    pedidoDAO.actualizarEstadoPago(cn, idPedido, "RECHAZADO");
                } else if ("PAGADO".equals(nuevoEstado) || "ENTREGADO".equals(nuevoEstado)) {
                    pedidoDAO.actualizarEstadoPago(cn, idPedido, "APROBADO");
                }
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    /** ENTRADA de mercadería: crea un lote nuevo con su fecha de vencimiento. */
    public int registrarEntrada(int idProducto, String numeroLote, int cantidad, Date vence, String motivo, int idUsuario)
            throws NegocioException, SQLException {
        if (cantidad <= 0) throw new NegocioException("La cantidad debe ser mayor que cero.");
        if (numeroLote == null || numeroLote.isBlank()) throw new NegocioException("Ingrese el número de lote.");
        if (vence == null) throw new NegocioException("Ingrese la fecha de vencimiento del lote.");
        try (Connection cn = ConexionBD.getConexion()) {
            cn.setAutoCommit(false);
            try {
                if (productoDAO.bloquear(cn, idProducto) == null) throw new NegocioException("El producto no existe.");
                Lote l = new Lote();
                l.setIdProducto(idProducto);
                l.setNumeroLote(numeroLote.trim());
                l.setCantidad(cantidad);
                l.setFechaVencimiento(vence);
                int idLote = loteDAO.insertar(cn, l);
                productoDAO.registrarMovimiento(cn, "ENTRADA", cantidad, motivo, idProducto, idUsuario, idLote);
                productoDAO.recalcularStock(cn, idProducto);
                cn.commit();
                return idLote;
            } catch (NegocioException | SQLException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    /**
     * SALIDA (merma, producto vencido) o AJUSTE (conteo físico: la cantidad es lo que realmente hay)
     * sobre un lote existente.
     */
    public void registrarSalidaOAjuste(int idLote, String tipo, int cantidad, String motivo, int idUsuario)
            throws NegocioException, SQLException {
        if (cantidad < 0 || (cantidad == 0 && "SALIDA".equals(tipo))) throw new NegocioException("La cantidad no es válida.");
        try (Connection cn = ConexionBD.getConexion()) {
            cn.setAutoCommit(false);
            try {
                Lote l = loteDAO.bloquear(cn, idLote);
                if (l == null) throw new NegocioException("Seleccione un lote.");
                int cambio;
                if ("SALIDA".equals(tipo)) {
                    if (cantidad > l.getCantidad()) throw new NegocioException("El lote " + l.getNumeroLote() + " solo tiene " + l.getCantidad() + " unidades.");
                    cambio = -cantidad;
                } else if ("AJUSTE".equals(tipo)) {
                    cambio = cantidad - l.getCantidad();
                    if (cambio == 0) throw new NegocioException("El conteo coincide con el sistema; no hay nada que ajustar.");
                } else {
                    throw new NegocioException("Tipo de movimiento no válido.");
                }
                loteDAO.sumarCantidad(cn, idLote, cambio);
                productoDAO.registrarMovimiento(cn, tipo, "SALIDA".equals(tipo) ? cantidad : cambio, motivo,
                        l.getIdProducto(), idUsuario, idLote);
                productoDAO.recalcularStock(cn, l.getIdProducto());
                cn.commit();
            } catch (NegocioException | SQLException e) {
                cn.rollback();
                throw e;
            }
        }
    }
}
