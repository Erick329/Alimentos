package com.lacosecha.controlador.admin;

import com.lacosecha.controlador.BaseServlet;
import com.lacosecha.dao.UsuarioDAO;
import com.lacosecha.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Clientes y usuarios: consulta, desbloqueo y activación de cuentas. */
@WebServlet("/admin/clientes")
public class ClientesServlet extends BaseServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("usuarios", usuarioDAO.listar(null));
            vista(req, resp, "admin/clientes", "Clientes y usuarios");
        } catch (SQLException e) {
            errorBD(req, resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = Validador.entero(req.getParameter("id"), 0);
        String accion = Validador.limpiar(req.getParameter("accion"));
        try {
            if (id == idUsuario(req)) {
                error(req, "No puede modificar su propia cuenta desde aquí.");
            } else if (accion.equals("desbloquear")) {
                usuarioDAO.reiniciarIntentos(id);
                exito(req, "Cuenta desbloqueada.");
            } else if (accion.equals("activar") || accion.equals("desactivar")) {
                usuarioDAO.cambiarStatus(id, accion.equals("activar") ? "ACTIVO" : "INACTIVO");
                exito(req, accion.equals("activar") ? "Cuenta activada." : "Cuenta desactivada.");
            }
            redirigir(req, resp, "/admin/clientes");
        } catch (SQLException e) {
            errorBD(req, resp, e);
        }
    }
}
