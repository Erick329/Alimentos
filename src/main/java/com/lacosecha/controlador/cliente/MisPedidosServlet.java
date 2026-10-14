package com.lacosecha.controlador.cliente;

import com.lacosecha.controlador.BaseServlet;
import com.lacosecha.dao.PedidoDAO;
import com.lacosecha.modelo.Pedido;
import com.lacosecha.modelo.Usuario;
import com.lacosecha.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Historial de pedidos del cliente y detalle de cada uno. */
@WebServlet(urlPatterns = {"/cliente/pedidos", "/cliente/pedido"})
public class MisPedidosServlet extends BaseServlet {

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Usuario u = usuario(req);
        try {
            if (req.getServletPath().equals("/cliente/pedido")) {
                Pedido p = pedidoDAO.buscar(Validador.entero(req.getParameter("id"), 0));
                // Un cliente solo puede ver SUS pedidos
                if (p == null || (p.getIdUsuario() != u.getIdUsuario() && !u.isPersonal())) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "El pedido no existe.");
                    return;
                }
                req.setAttribute("pedido", p);
                req.setAttribute("nuevo", "1".equals(req.getParameter("nuevo")));
                vista(req, resp, "cliente/pedido", "Pedido #" + p.getIdPedido());
            } else {
                req.setAttribute("pedidos", pedidoDAO.listarPorUsuario(u.getIdUsuario()));
                vista(req, resp, "cliente/pedidos", "Mis pedidos");
            }
        } catch (SQLException e) {
            errorBD(req, resp, e);
        }
    }
}
