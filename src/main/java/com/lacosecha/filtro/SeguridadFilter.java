package com.lacosecha.filtro;

import com.lacosecha.util.Seguridad;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Primer filtro de todas las peticiones:
 *  - Codificación UTF-8 (tildes y ñ).
 *  - Cabeceras de seguridad (anti-XSS / clickjacking).
 *  - Token CSRF: se crea por sesión y se verifica en cada POST.
 */
public class SeguridadFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Referrer-Policy", "same-origin");
        resp.setHeader("Content-Security-Policy",
                "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; "
              + "font-src 'self'; img-src 'self' data: https:; frame-ancestors 'none'");

        String ruta = req.getServletPath();
        boolean estatico = ruta.startsWith("/css/") || ruta.startsWith("/js/") || ruta.startsWith("/img/") || ruta.startsWith("/lib/");
        if (!estatico) {
            HttpSession sesion = req.getSession(true);
            if (sesion.getAttribute("csrf") == null) sesion.setAttribute("csrf", Seguridad.token(24));

            if ("POST".equalsIgnoreCase(req.getMethod())) {
                String enviado = req.getParameter("csrf");
                if (enviado == null || !enviado.equals(sesion.getAttribute("csrf"))) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Solicitud no válida (token CSRF). Recargue la página e intente de nuevo.");
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }
}
