package com.lacosecha.controlador;

import com.lacosecha.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Recuperación de contraseña (SIMULADA): siempre muestra el mismo mensaje
 * para no revelar qué correos están registrados.
 */
@WebServlet("/recuperar")
public class RecuperarServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        vista(req, resp, "publico/recuperar", "Recuperar contraseña");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = Validador.limpiar(req.getParameter("email"));
        exito(req, "Si el correo está registrado, recibirá instrucciones para restablecer su contraseña.");
        redirigir(req, resp, "/login");
    }
}
