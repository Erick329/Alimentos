package com.lacosecha.controlador.admin;

import com.lacosecha.controlador.BaseServlet;
import com.lacosecha.dao.LoteDAO;
import com.lacosecha.dao.ProductoDAO;
import com.lacosecha.servicio.PedidoService;
import com.lacosecha.util.NegocioException;
import com.lacosecha.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Inventario por LOTES (solo ADMIN):
 *  - ENTRADA: crea un lote nuevo con número y fecha de vencimiento.
 *  - SALIDA:  merma o producto vencido, sobre un lote existente.
 *  - AJUSTE:  conteo físico de un lote (la cantidad es lo que realmente hay).
 * Cada operación queda registrada en la tabla Movimientos y recalcula Productos.stock.
 */
@WebServlet("/admin/inventario")
public class InventarioServlet extends BaseServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final LoteDAO loteDAO = new LoteDAO();
    private final PedidoService pedidoService = new PedidoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int productoSel = Validador.entero(req.getParameter("producto"), 0);
            req.setAttribute("productos", productoDAO.listar(0, null, false));
            req.setAttribute("productoSel", productoSel);
            if (productoSel > 0) {
                req.setAttribute("producto", productoDAO.buscar(productoSel));
                req.setAttribute("lotes", loteDAO.listarPorProducto(productoSel));
            }
            req.setAttribute("porVencer", loteDAO.proximosAVencer(30));
            req.setAttribute("bajoStock", productoDAO.bajoStock());
            req.setAttribute("movimientos", productoDAO.ultimosMovimientos(25));
            vista(req, resp, "admin/inventario", "Inventario por lotes");
        } catch (SQLException e) {
            errorBD(req, resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String tipo = Validador.limpiar(req.getParameter("tipo"));
        int idProducto = Validador.entero(req.getParameter("idProducto"), 0);
        int cantidad = Validador.entero(req.getParameter("cantidad"), -1);
        String motivo = Validador.limpiar(req.getParameter("motivo"));
        if (motivo.isEmpty()) motivo = tipo.equals("ENTRADA") ? "Compra a proveedor" : tipo.equals("SALIDA") ? "Merma" : "Conteo físico";
        if (motivo.length() > 150) motivo = motivo.substring(0, 150);
        try {
            if (tipo.equals("ENTRADA")) {
                String numeroLote = Validador.limpiar(req.getParameter("numeroLote")).toUpperCase();
                java.sql.Date vence;
                try {
                    vence = java.sql.Date.valueOf(Validador.limpiar(req.getParameter("fechaVencimiento")));
                } catch (IllegalArgumentException e) {
                    vence = null;
                }
                if (numeroLote.length() > 50) throw new NegocioException("El número de lote admite máximo 50 caracteres.");
                if (vence != null && !vence.toLocalDate().isAfter(java.time.LocalDate.now()))
                    throw new NegocioException("La fecha de vencimiento de un lote nuevo debe ser posterior a hoy.");
                pedidoService.registrarEntrada(idProducto, numeroLote, cantidad, vence, motivo, idUsuario(req));
                exito(req, "Lote " + numeroLote + " registrado (+" + cantidad + " unidades).");
            } else {
                int idLote = Validador.entero(req.getParameter("idLote"), 0);
                pedidoService.registrarSalidaOAjuste(idLote, tipo, cantidad, motivo, idUsuario(req));
                exito(req, (tipo.equals("SALIDA") ? "Salida" : "Ajuste") + " registrado en el lote.");
            }
        } catch (NegocioException e) {
            error(req, e.getMessage());
        } catch (SQLException e) {
            errorBD(req, resp, e);
            return;
        }
        redirigir(req, resp, "/admin/inventario" + (idProducto > 0 ? "?producto=" + idProducto : ""));
    }
}
