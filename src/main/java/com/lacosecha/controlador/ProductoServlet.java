package com.lacosecha.controlador;

import com.lacosecha.dao.ProductoDAO;
import com.lacosecha.modelo.Producto;
import com.lacosecha.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Detalle de un producto. */
@WebServlet("/producto")
public class ProductoServlet extends BaseServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Producto p = productoDAO.buscar(Validador.entero(req.getParameter("id"), 0));
            if (p == null || !p.isActivo()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "El producto no existe.");
                return;
            }
            req.setAttribute("producto", p);
            req.setAttribute("relacionados", productoDAO.listar(p.getIdCategoria(), null, true)
                    .stream().filter(x -> x.getIdProducto() != p.getIdProducto()).limit(4).toList());
            req.setAttribute("enCarrito", carrito(req).cantidadDe(p.getIdProducto()));
            vista(req, resp, "publico/producto", p.getNombre());
        } catch (SQLException e) {
            errorBD(req, resp, e);
        }
    }
}
