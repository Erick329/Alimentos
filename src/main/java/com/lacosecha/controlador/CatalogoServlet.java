package com.lacosecha.controlador;

import com.lacosecha.dao.CategoriaDAO;
import com.lacosecha.dao.ProductoDAO;
import com.lacosecha.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Catálogo con filtro por categoría y búsqueda por nombre o código. */
@WebServlet("/catalogo")
public class CatalogoServlet extends BaseServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int categoria = Validador.entero(req.getParameter("categoria"), 0);
        String q = Validador.limpiar(req.getParameter("q"));
        if (q.length() > 60) q = q.substring(0, 60);
        try {
            req.setAttribute("productos", productoDAO.listar(categoria, q, true));
            req.setAttribute("categorias", categoriaDAO.listar(true));
            req.setAttribute("categoriaSel", categoria);
            req.setAttribute("q", q);
            vista(req, resp, "publico/catalogo", "Catálogo");
        } catch (SQLException e) {
            errorBD(req, resp, e);
        }
    }
}
