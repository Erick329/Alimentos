package com.lacosecha.controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** Cierra la sesión (por POST para que esté protegido con el token CSRF). */
@WebServlet("/logout")
public class LogoutServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer id = idUsuario(req);
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();
        redirigir(req, resp, "/inicio");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        redirigir(req, resp, "/inicio");
    }
}
