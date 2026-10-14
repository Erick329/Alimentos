package com.lacosecha.controlador;

import com.lacosecha.dao.UsuarioDAO;
import com.lacosecha.modelo.Usuario;
import com.lacosecha.util.Seguridad;
import com.lacosecha.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Inicio de sesión con BCrypt, bloqueo tras 5 intentos fallidos,
 * cambio de ID de sesión (evita fijación de sesión).
 */
@WebServlet("/login")
public class LoginServlet extends BaseServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (usuario(req) != null) {
            redirigir(req, resp, "/inicio");
            return;
        }
        req.setAttribute("next", rutaSegura(req.getParameter("next"), ""));
        vista(req, resp, "publico/login", "Iniciar sesión");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = Validador.limpiar(req.getParameter("email")).toLowerCase();
        String password = req.getParameter("password");
        String next = rutaSegura(req.getParameter("next"), "");
        try {
            Usuario u = usuarioDAO.buscarPorEmail(email);
            String mensaje = null;
            if (u == null) {
                mensaje = "Correo o contraseña incorrectos.";
            } else if (!u.isActivo()) {
                mensaje = "Su cuenta está desactivada. Contacte a la empresa.";
            } else if (u.isBloqueado()) {
                mensaje = "Su cuenta está bloqueada por demasiados intentos fallidos. Contacte al administrador.";
            } else if (!Seguridad.verificarPassword(password, u.getPassword())) {
                usuarioDAO.registrarIntentoFallido(u.getIdUsuario());
                int restantes = Usuario.MAX_INTENTOS - (u.getIntentosFallidos() + 1);
                mensaje = restantes > 0
                        ? "Correo o contraseña incorrectos. Intentos restantes: " + restantes + "."
                        : "Su cuenta fue bloqueada por demasiados intentos fallidos.";
            }
            if (mensaje != null) {
                req.setAttribute("error", mensaje);
                req.setAttribute("email", email);
                req.setAttribute("next", next);
                vista(req, resp, "publico/login", "Iniciar sesión");
                return;
            }

            // Inicio de sesión correcto
            usuarioDAO.reiniciarIntentos(u.getIdUsuario());
            u.setPassword(null);                // no se guarda el hash en la sesión
            req.changeSessionId();                  // nuevo ID de sesión (conserva el carrito)
            req.getSession().setAttribute("usuario", u);

            if (!next.isEmpty()) redirigir(req, resp, next);
            else if (u.isPersonal()) redirigir(req, resp, "/admin/dashboard");
            else redirigir(req, resp, "/inicio");
        } catch (SQLException e) {
            errorBD(req, resp, e);
        }
    }
}
