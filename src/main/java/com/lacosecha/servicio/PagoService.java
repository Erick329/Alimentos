package com.lacosecha.servicio;

import com.lacosecha.util.Seguridad;
import java.math.BigDecimal;

/**
 * Pasarela de pagos SIMULADA (modo pruebas / sandbox).
 * En producción esta clase llamaría por HTTPS a una pasarela certificada PCI DSS.
 * Nunca se guardan los datos de la tarjeta: solo la referencia que devuelve la pasarela.
 *
 * Tarjetas de prueba: 4111 1111 1111 1111 = APROBADA; 4000 0000 0000 0002 = RECHAZADA (fondos insuficientes).
 */
public class PagoService {

    public static class ResultadoPago {
        private final boolean aprobado;
        private final String estado;
        private final String referencia;
        private final String mensaje;

        public ResultadoPago(boolean aprobado, String estado, String referencia, String mensaje) {
            this.aprobado = aprobado;
            this.estado = estado;
            this.referencia = referencia;
            this.mensaje = mensaje;
        }
        public boolean isAprobado() { return aprobado; }
        public String getEstado() { return estado; }
        public String getReferencia() { return referencia; }
        public String getMensaje() { return mensaje; }
    }

    public ResultadoPago procesar(String metodo, BigDecimal monto, String numeroTarjeta,
                                  String vencimiento, String cvv) {
        String ref = "SIM-" + Seguridad.token(6).toUpperCase();
        switch (metodo) {
            case "TARJETA":
                String numero = numeroTarjeta == null ? "" : numeroTarjeta.replaceAll("[\\s-]", "");
                if (!numero.matches("\\d{13,19}") || !luhn(numero)) {
                    return new ResultadoPago(false, "RECHAZADO", null, "El número de tarjeta no es válido.");
                }
                if (vencimiento == null || !vencimiento.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                    return new ResultadoPago(false, "RECHAZADO", null, "La fecha de vencimiento debe tener el formato MM/AA.");
                }
                if (cvv == null || !cvv.matches("\\d{3,4}")) {
                    return new ResultadoPago(false, "RECHAZADO", null, "El código de seguridad (CVV) no es válido.");
                }
                if (numero.equals("4000000000000002")) {
                    return new ResultadoPago(false, "RECHAZADO", null, "La pasarela rechazó el pago (fondos insuficientes).");
                }
                String ultimos = numero.substring(numero.length() - 4);
                return new ResultadoPago(true, "APROBADO", ref + " ****" + ultimos, "Pago aprobado.");
            case "TRANSFERENCIA":
                return new ResultadoPago(true, "PENDIENTE", ref, "Pendiente de confirmar la transferencia.");
            case "EFECTIVO":
                return new ResultadoPago(true, "PENDIENTE", ref, "Se cobrará al entregar el pedido.");
            default:
                return new ResultadoPago(false, "RECHAZADO", null, "Método de pago no válido.");
        }
    }

    /** Algoritmo de Luhn: valida el dígito verificador de una tarjeta. */
    static boolean luhn(String numero) {
        int suma = 0;
        boolean doble = false;
        for (int i = numero.length() - 1; i >= 0; i--) {
            int d = numero.charAt(i) - '0';
            if (doble) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            suma += d;
            doble = !doble;
        }
        return suma % 10 == 0;
    }
}
