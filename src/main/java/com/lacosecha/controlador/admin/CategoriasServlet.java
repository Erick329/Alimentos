package com.lacosecha.controlador.admin;

import com.lacosecha.controlador.BaseServlet;
import com.lacosecha.dao.CategoriaDAO;
import com.lacosecha.modelo.Categoria;
import com.lacosecha.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/** Administración de categorías. */
@WebServlet("/admin/categorias")
public class CategoriasServlet extends BaseServlet {

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int id = Validador.entero(req.getParameter("id"), 0);
            Categoria c = id > 0 ? categoriaDAO.buscar(id) : null;
            req.setAttribute("form", c == null ? new Categoria() : c);
            req.setAttribute("categorias", categoriaDAO.listar(false));
            vista(req, resp, "admin/categorias", "Categorías");
        } catch (SQLException e) {
            errorBD(req, resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Categoria c = new Categoria();
        c.setIdCategoria(Validador.entero(req.getParameter("id"), 0));
        c.setNombre(Validador.limpiar(req.getParameter("nombre")));
        c.setDescripcion(Validador.limpiar(req.getParameter("descripcion")));
        c.setStatus(req.getParameter("activo") != null ? "ACTIVO" : "INACTIVO");
        if (Validador.vacio(c.getNombre()) || c.getNombre().length() > 100
                || Validador.vacio(c.getDescripcion()) || c.getDescripcion().length() > 255) {
            error(req, "Ingrese el nombre (máximo 100 caracteres) y la descripción de la categoría.");
            redirigir(req, resp, "/admin/categorias" + (c.getIdCategoria() > 0 ? "?id=" + c.getIdCategoria() : ""));
            return;
        }
        try {
            categoriaDAO.guardar(c);
            exito(req, "Categoría \"" + c.getNombre() + "\" guardada.");
        } catch (SQLIntegrityConstraintViolationException e) {
            error(req, "Ya existe una categoría con ese nombre.");
        } catch (SQLException e) {
            errorBD(req, resp, e);
            return;
        }
        redirigir(req, resp, "/admin/categorias");
    }
}
