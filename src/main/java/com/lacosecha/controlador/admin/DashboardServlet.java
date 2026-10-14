package com.lacosecha.controlador.admin;

import com.lacosecha.controlador.BaseServlet;
import com.lacosecha.dao.PedidoDAO;
import com.lacosecha.dao.ProductoDAO;
import com.lacosecha.dao.ReporteDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

/** Panel principal: indicadores de hoy y de los últimos 30 días, pedidos recientes y alertas. */
@WebServlet("/admin/dashboard")
public class DashboardServlet extends BaseServlet {

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LocalDate hoy = LocalDate.now();
        try {
            req.setAttribute("hoy", reporteDAO.resumen(hoy, hoy));
            req.setAttribute("mes", reporteDAO.resumen(hoy.minusDays(29), hoy));
            req.setAttribute("recientes", pedidoDAO.listar(null, 8));
            req.setAttribute("bajoStock", productoDAO.bajoStock());
            req.setAttribute("porVencer", new com.lacosecha.dao.LoteDAO().proximosAVencer(15));
            req.setAttribute("porAtender", pedidoDAO.listar("PAGADO", 50).size() + pedidoDAO.listar("PENDIENTE", 50).size());
            vista(req, resp, "admin/dashboard", "Dashboard");
        } catch (SQLException e) {
            errorBD(req, resp, e);
        }
    }
}
