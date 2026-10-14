package com.lacosecha.controlador;

import com.lacosecha.dao.ProductoDAO;
import com.lacosecha.modelo.Carrito;
import com.lacosecha.modelo.Producto;
import com.lacosecha.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Carrito de compras (se guarda en la sesión). */
@WebServlet("/carrito")
public class CarritoServlet extends BaseServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        carrito(req);
        vista(req, resp, "publico/carrito", "Carrito de compras");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String accion = Validador.limpiar(req.getParameter("accion"));
        int idProducto = Validador.entero(req.getParameter("idProducto"), 0);
        int cantidad = Validador.entero(req.getParameter("cantidad"), 1);
        Carrito carrito = carrito(req);
        String volver = rutaSegura(req.getParameter("volver"), "/carrito");

        try {
            switch (accion) {
                case "agregar": {
                    Producto p = productoDAO.buscar(idProducto);
                    if (p == null || !p.isActivo()) {
                        error(req, "El producto no está disponible.");
                    } else if (cantidad < 1 || cantidad > 99) {
                        error(req, "La cantidad debe estar entre 1 y 99.");
                    } else if (carrito.cantidadDe(idProducto) + cantidad > p.getStock()) {
                        error(req, "Solo hay " + p.getStock() + " unidades disponibles de " + p.getNombre() + ".");
                    } else {
                        carrito.agregar(p, cantidad);
                        exito(req, p.getNombre() + " se agregó al carrito.");
                    }
                    break;
                }
                case "actualizar": {
                    Producto p = productoDAO.buscar(idProducto);
                    if (p != null && cantidad > p.getStock()) {
                        error(req, "Solo hay " + p.getStock() + " unidades disponibles de " + p.getNombre() + ".");
                    } else if (cantidad < 0 || cantidad > 99) {
                        error(req, "La cantidad debe estar entre 0 y 99.");
                    } else {
                        carrito.actualizar(idProducto, cantidad);
                    }
                    volver = "/carrito";
                    break;
                }
                case "eliminar":
                    carrito.eliminar(idProducto);
                    volver = "/carrito";
                    break;
                case "vaciar":
                    carrito.vaciar();
                    volver = "/carrito";
                    break;
                default:
                    volver = "/carrito";
            }
            redirigir(req, resp, volver);
        } catch (SQLException e) {
            errorBD(req, resp, e);
        }
    }
}
