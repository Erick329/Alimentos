package com.lacosecha.controlador;

import com.lacosecha.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Páginas informativas: Nosotros y Contacto. */
@WebServlet(urlPatterns = {"/nosotros", "/contacto"})
public class PaginaServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getServletPath().equals("/nosotros")) {
            vista(req, resp, "publico/nosotros", "Nosotros");
        } else {
            vista(req, resp, "publico/contacto", "Contacto");
        }
    }

    /** Recibe el formulario de contacto (simulado: el mensaje se muestra en la consola del servidor). */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String nombre = Validador.limpiar(req.getParameter("nombre"));
        String email = Validador.limpiar(req.getParameter("email"));
        String mensaje = Validador.limpiar(req.getParameter("mensaje"));
        if (Validador.vacio(nombre) || !Validador.email(email) || mensaje.length() < 10) {
            req.setAttribute("error", "Complete su nombre, un correo válido y un mensaje de al menos 10 caracteres.");
            req.setAttribute("nombre", nombre);
            req.setAttribute("email", email);
            req.setAttribute("mensaje", mensaje);
            vista(req, resp, "publico/contacto", "Contacto");
            return;
        }
        System.out.println("[Contacto] " + nombre + " <" + email + ">: " + mensaje);
        exito(req, "¡Gracias, " + nombre + "! Recibimos su mensaje y le responderemos pronto.");
        redirigir(req, resp, "/contacto");
    }
}
