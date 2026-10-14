package com.lacosecha.controlador;

import com.lacosecha.dao.CategoriaDAO;
import com.lacosecha.dao.ProductoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Página de inicio: categorías y productos más vendidos. */
@WebServlet(urlPatterns = {"", "/inicio"})
public class InicioServlet extends BaseServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("destacados", productoDAO.destacados(8));
            req.setAttribute("categorias", categoriaDAO.listar(true));
            vista(req, resp, "publico/inicio", "Inicio");
        } catch (SQLException e) {
            errorBD(req, resp, e);
        }
    }
}
