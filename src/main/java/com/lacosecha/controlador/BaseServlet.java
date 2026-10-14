package com.lacosecha.controlador;

import com.lacosecha.modelo.Carrito;
import com.lacosecha.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/** Métodos comunes para todos los controladores. */
public abstract class BaseServlet extends HttpServlet {


    /** Muestra una vista JSP ubicada en /WEB-INF/vistas/ (no accesible directamente desde el navegador). */
    protected void vista(HttpServletRequest req, HttpServletResponse resp, String vista, String titulo)
            throws ServletException, IOException {
        req.setAttribute("titulo", titulo);
        req.getRequestDispatcher("/WEB-INF/vistas/" + vista + ".jsp").forward(req, resp);
    }

    protected void redirigir(HttpServletRequest req, HttpServletResponse resp, String ruta) throws IOException {
        resp.sendRedirect(req.getContextPath() + ruta);
    }

    protected Usuario usuario(HttpServletRequest req) {
        return (Usuario) req.getSession().getAttribute("usuario");
    }

    protected Carrito carrito(HttpServletRequest req) {
        HttpSession s = req.getSession();
        Carrito c = (Carrito) s.getAttribute("carrito");
        if (c == null) {
            c = new Carrito();
            s.setAttribute("carrito", c);
        }
        return c;
    }

    /** Mensajes que se muestran una sola vez en la siguiente página. */
    protected void exito(HttpServletRequest req, String msg) { req.getSession().setAttribute("flashOk", msg); }
    protected void error(HttpServletRequest req, String msg) { req.getSession().setAttribute("flashError", msg); }

    /** Evita redirecciones abiertas: solo se permiten rutas internas. */
    protected String rutaSegura(String ruta, String porDefecto) {
        if (ruta == null || !ruta.startsWith("/") || ruta.startsWith("//") || ruta.contains("\\")) return porDefecto;
        return ruta;
    }

    protected String ip(HttpServletRequest req) { return req.getRemoteAddr(); }

    protected Integer idUsuario(HttpServletRequest req) {
        Usuario u = usuario(req);
        return u == null ? null : u.getIdUsuario();
    }

    /** Registra el error técnico y muestra una página amigable (no se exponen detalles al usuario). */
    protected void errorBD(HttpServletRequest req, HttpServletResponse resp, SQLException e) throws IOException {
        log("Error de base de datos", e);
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "No fue posible conectarse a la base de datos. Verifique que MySQL esté iniciado y la configuración en db.properties.");
    }
}
