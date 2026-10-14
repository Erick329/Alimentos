package com.lacosecha.filtro;

import com.lacosecha.modelo.Usuario;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Control de acceso por roles:
 *  /cliente/*  -> cualquier usuario con sesión iniciada.
 *  /admin/*    -> ADMIN (todo) o VENDEDOR (solo dashboard y pedidos).
 */
public class AutenticacionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        Usuario u = (Usuario) req.getSession().getAttribute("usuario");
        String ruta = req.getServletPath();

        if (u == null) {
            String volver = ruta + (req.getQueryString() != null ? "?" + req.getQueryString() : "");
            if (!"GET".equalsIgnoreCase(req.getMethod())) volver = "/inicio";
            resp.sendRedirect(req.getContextPath() + "/login?next=" + URLEncoder.encode(volver, StandardCharsets.UTF_8));
            return;
        }
        if (ruta.startsWith("/admin/")) {
            boolean permitido = u.isAdmin()
                    || (u.isVendedor() && (ruta.equals("/admin/dashboard") || ruta.equals("/admin/pedidos")));
            if (!permitido) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "No tiene permisos para acceder a esta sección.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
