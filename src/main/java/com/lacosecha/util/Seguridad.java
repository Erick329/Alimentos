package com.lacosecha.util;

import java.security.SecureRandom;
import java.util.Base64;
import org.mindrot.jbcrypt.BCrypt;

/** Utilidades de seguridad: hash de contraseñas y tokens aleatorios. */
public final class Seguridad {

    private static final SecureRandom RANDOM = new SecureRandom();

    private Seguridad() { }

    /** Genera el hash BCrypt de una contraseña (nunca se guarda en texto plano). */
    public static String hashPassword(String plano) {
        return BCrypt.hashpw(plano, BCrypt.gensalt(12));
    }

    /** Compara la contraseña ingresada contra el hash guardado. */
    public static boolean verificarPassword(String plano, String hash) {
        if (plano == null || hash == null || !hash.startsWith("$2")) return false;
        try {
            return BCrypt.checkpw(plano, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Token aleatorio seguro (para CSRF y referencias). */
    public static String token(int bytes) {
        byte[] b = new byte[bytes];
        RANDOM.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    /** Pequeño programa para generar hashes desde NetBeans (Run File). */
    public static void main(String[] args) {
        String clave = args.length > 0 ? args[0] : "Admin123*";
        System.out.println(clave + " -> " + hashPassword(clave));
    }
}
