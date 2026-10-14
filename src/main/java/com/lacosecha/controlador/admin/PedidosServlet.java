package com.lacosecha.controlador.admin;

import com.lacosecha.controlador.BaseServlet;
import com.lacosecha.dao.PedidoDAO;
import com.lacosecha.modelo.Pedido;
import com.lacosecha.servicio.PedidoService;
import com.lacosecha.util.NegocioException;
import com.lacosecha.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

/** Gestión de pedidos: listado por estado, detalle y cambio de estado (ADMIN y VENDEDOR). */
@WebServlet("/admin/pedidos")
public class PedidosServlet extends BaseServlet {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final PedidoService pedidoService = new PedidoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int id = Validador.entero(req.getParameter("id"), 0);
            if (id > 0) {
                Pedido p = pedidoDAO.buscar(id);
                if (p == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "El pedido no existe.");
                    return;
                }
                req.setAttribute("pedido", p);
                req.setAttribute("siguientes", PedidoService.siguientesEstados(p.getEstado()));
                vista(req, resp, "admin/pedido", "Pedido #" + id);
                return;
            }
            String estado = Validador.limpiar(req.getParameter("estado"));
            if (!Arrays.asList(Pedido.ESTADOS).contains(estado)) estado = null;
            req.setAttribute("pedidos", pedidoDAO.listar(estado, 200));
            req.setAttribute("estadoSel", estado);
            req.setAttribute("estados", Pedido.ESTADOS);
            vista(req, resp, "admin/pedidos", "Pedidos");
        } catch (SQLException e) {
            errorBD(req, resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = Validador.entero(req.getParameter("id"), 0);
        String estado = Validador.limpiar(req.getParameter("estado"));
        try {
            pedidoService.cambiarEstado(id, estado, idUsuario(req));
            exito(req, "El pedido #" + id + " cambió a " + estado.replace('_', ' ') + ".");
        } catch (NegocioException e) {
            error(req, e.getMessage());
        } catch (SQLException e) {
            errorBD(req, resp, e);
            return;
        }
        redirigir(req, resp, "/admin/pedidos?id=" + id);
    }
}
