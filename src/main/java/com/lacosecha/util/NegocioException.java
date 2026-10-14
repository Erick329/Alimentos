package com.lacosecha.util;

/** Error de regla de negocio con un mensaje apto para mostrar al usuario. */
public class NegocioException extends Exception {
    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
