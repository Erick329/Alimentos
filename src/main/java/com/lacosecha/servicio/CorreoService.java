package com.lacosecha.servicio;

import com.lacosecha.modelo.Pedido;
import com.lacosecha.modelo.Usuario;

/**
 * Envío de correos SIMULADO: escribe el mensaje en la consola del servidor (pestaña Output de NetBeans).
 * Para enviar correos reales se puede usar Jakarta Mail con una cuenta SMTP.
 */
public class CorreoService {

    public void enviarConfirmacion(Usuario u, Pedido p) {
        System.out.println("========== CORREO SIMULADO ==========");
        System.out.println("Para: " + u.getEmail());
        System.out.println("Asunto: Confirmación de su pedido #" + p.getIdPedido());
        System.out.println("Hola " + u.getNombre() + ", recibimos su pedido por Q" + p.getTotal()
                + ". Estado: " + p.getEstado() + ". ¡Gracias por su compra!");
        System.out.println("=====================================");
    }
}
