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
import java.util.ArrayList;
import java.util.List;

/** Registro de nuevos clientes. */
@WebServlet("/registro")
public class RegistroServlet extends BaseServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("next", rutaSegura(req.getParameter("next"), ""));
        vista(req, resp, "publico/registro", "Crear cuenta");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Usuario u = new Usuario();
        u.setNombre(Validador.limpiar(req.getParameter("nombre")));
        u.setApellido(Validador.limpiar(req.getParameter("apellido")));
        u.setEmail(Validador.limpiar(req.getParameter("email")).toLowerCase());
        u.setTelefono(Validador.limpiar(req.getParameter("telefono")));
        u.setDireccion(Validador.limpiar(req.getParameter("direccion")));
        String nit = Validador.limpiar(req.getParameter("nit")).toUpperCase().replace(" ", "");
        u.setNit(nit.isEmpty() ? "CF" : nit);
        String password = req.getParameter("password");
        String confirmar = req.getParameter("confirmar");
        String next = rutaSegura(req.getParameter("next"), "");

        List<String> errores = new ArrayList<>();
        if (Validador.vacio(u.getNombre()) || u.getNombre().length() > 60) errores.add("Ingrese su nombre.");
        if (Validador.vacio(u.getApellido()) || u.getApellido().length() > 60) errores.add("Ingrese su apellido.");
        if (!Validador.email(u.getEmail())) errores.add("Ingrese un correo electrónico válido.");
        if (!Validador.nit(u.getNit())) errores.add("NIT inválido (ej. 1234567-8) o escriba CF.");
        if (Validador.vacio(u.getDireccion()) || u.getDireccion().length() > 255) errores.add("Ingrese su dirección.");
        if (!Validador.telefono(u.getTelefono())) errores.add("El teléfono solo puede tener números (8 dígitos o más).");
        if (!Validador.passwordSegura(password)) errores.add("La contraseña debe tener al menos 8 caracteres, con letras y números.");
        else if (!password.equals(confirmar)) errores.add("Las contraseñas no coinciden.");

        try {
            if (errores.isEmpty() && usuarioDAO.existeEmail(u.getEmail())) errores.add("Ya existe una cuenta con ese correo.");
            if (!errores.isEmpty()) {
                req.setAttribute("errores", errores);
                req.setAttribute("form", u);
                req.setAttribute("next", next);
                vista(req, resp, "publico/registro", "Crear cuenta");
                return;
            }
            u.setPassword(Seguridad.hashPassword(password));
            int id = usuarioDAO.registrarCliente(u);
            Usuario nuevo = usuarioDAO.buscarPorId(id);
            nuevo.setPassword(null);
            req.changeSessionId();
            req.getSession().setAttribute("usuario", nuevo);
            exito(req, "¡Bienvenido(a), " + nuevo.getNombre() + "! Su cuenta fue creada.");
            redirigir(req, resp, next.isEmpty() ? "/catalogo" : next);
        } catch (SQLException e) {
            errorBD(req, resp, e);
        }
    }
}
