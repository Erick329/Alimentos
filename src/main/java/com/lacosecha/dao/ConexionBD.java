package com.lacosecha.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Crea conexiones JDBC a MySQL leyendo los datos de src/main/resources/db.properties.
 * Cada DAO abre una conexión con try-with-resources y la cierra al terminar.
 */

public final class ConexionBD {

        private static final Properties CFG = new Properties();

        static {
            try (InputStream in = ConexionBD.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in == null) throw new IllegalStateException("No se encontro db.properties");
                CFG.load(in);
            } catch (Exception e) {
                throw new ExceptionInInitializerError("Error al leer la configuracion de la BD: " + e.getMessage());
            }
            String driver = CFG.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            try {
                Class.forName(driver);   // registra el driver de MySQL (necesario dentro de Tomcat)
            } catch (ClassNotFoundException e) {
                System.err.println("[ConexionBD] No se encontro el driver " + driver + ". Revise la dependencia en pom.xml.");
            }
        }

        private ConexionBD() { }

        public static Connection getConexion() throws SQLException {
            return DriverManager.getConnection(
                    CFG.getProperty("db.url"),
                    CFG.getProperty("db.user"),
                    CFG.getProperty("db.password", ""));
        }

        /** Prueba rápida de conexión: clic derecho > Run File en NetBeans. */
        public static void main(String[] args) {
            try (Connection cn = getConexion()) {
                System.out.println("Conexion exitosa a: " + cn.getMetaData().getURL());
            } catch (SQLException e) {
                System.out.println("Error de conexion: " + e.getMessage());
            }
    }
}
