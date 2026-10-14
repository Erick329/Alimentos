package com.lacosecha.dao;

import com.lacosecha.modelo.FilaReporte;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Consultas para los reportes de toma de decisiones.
 * Los pedidos CANCELADOS no se consideran ventas.
 */
public class ReporteDAO {

    private static final String VENTA_VALIDA = "pe.status <> 'CANCELADO' AND pe.fechaPedido >= ? AND pe.fechaPedido < ? ";

    /** Indicadores principales del período: ventas, pedidos, ticket promedio, clientes y unidades. */
    public Map<String, Object> resumen(LocalDate desde, LocalDate hasta) throws SQLException {
        Map<String, Object> r = new LinkedHashMap<>();
        String sql = "SELECT COUNT(*) AS pedidos, COALESCE(SUM(pe.total), 0) AS ventas, "
                   + "COALESCE(AVG(pe.total), 0) AS ticket, COUNT(DISTINCT pe.idUsuario) AS clientes "
                   + "FROM Pedidos pe WHERE " + VENTA_VALIDA;
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            fechas(ps, 1, desde, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                r.put("pedidos", rs.getLong("pedidos"));
                r.put("ventas", rs.getBigDecimal("ventas"));
                r.put("ticket", rs.getBigDecimal("ticket").setScale(2, java.math.RoundingMode.HALF_UP));
                r.put("clientes", rs.getLong("clientes"));
            }
        }
        String sqlU = "SELECT COALESCE(SUM(d.cantidad), 0) FROM DetallePedido d JOIN Pedidos pe ON pe.idPedido = d.idPedido WHERE " + VENTA_VALIDA;
        r.put("unidades", escalar(sqlU, desde, hasta));
        r.put("pendientes", escalar("SELECT COUNT(*) FROM Pedidos pe WHERE pe.status IN ('PENDIENTE','PAGADO','EN_PREPARACION') "
                + "AND pe.fechaPedido >= ? AND pe.fechaPedido < ?", desde, hasta));
        return r;
    }

    /** R1. Ventas agrupadas por mes (o por día si el rango es corto). */
    public List<FilaReporte> ventasPorPeriodo(LocalDate desde, LocalDate hasta) throws SQLException {
        boolean porDia = desde.plusDays(45).isAfter(hasta);
        String formato = porDia ? "%Y-%m-%d" : "%Y-%m";
        String sql = "SELECT DATE_FORMAT(pe.fechaPedido, '" + formato + "') AS periodo, SUM(pe.total) AS monto, COUNT(*) AS cant "
                   + "FROM Pedidos pe WHERE " + VENTA_VALIDA
                   + "GROUP BY DATE_FORMAT(pe.fechaPedido, '" + formato + "') ORDER BY periodo";
        return lista(sql, desde, hasta);
    }

    /** R2. Top 10 de productos más vendidos. */
    public List<FilaReporte> topProductos(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT p.nombre AS periodo, SUM(d.subtotal) AS monto, SUM(d.cantidad) AS cant "
                   + "FROM DetallePedido d JOIN Productos p ON p.idProducto = d.idProducto "
                   + "JOIN Pedidos pe ON pe.idPedido = d.idPedido WHERE " + VENTA_VALIDA
                   + "GROUP BY p.idProducto, p.nombre ORDER BY cant DESC LIMIT 10";
        return lista(sql, desde, hasta);
    }

    /** R3. Productos activos con menos ventas en el período (baja rotación). */
    public List<FilaReporte> bajaRotacion(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT p.nombre AS periodo, p.stock AS monto, "
                   + "(SELECT COALESCE(SUM(d.cantidad), 0) FROM DetallePedido d JOIN Pedidos pe ON pe.idPedido = d.idPedido "
                   + " WHERE d.idProducto = p.idProducto AND " + VENTA_VALIDA + ") AS cant "
                   + "FROM Productos p WHERE p.status = 'ACTIVO' ORDER BY cant ASC, p.stock DESC LIMIT 5";
        return lista(sql, desde, hasta);
    }

    /** R4. Participación de cada categoría en las ventas. */
    public List<FilaReporte> ventasPorCategoria(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT c.nombre AS periodo, SUM(d.subtotal) AS monto, SUM(d.cantidad) AS cant "
                   + "FROM DetallePedido d JOIN Productos p ON p.idProducto = d.idProducto "
                   + "JOIN Categorias c ON c.idCategoria = p.idCategoria "
                   + "JOIN Pedidos pe ON pe.idPedido = d.idPedido WHERE " + VENTA_VALIDA
                   + "GROUP BY c.idCategoria, c.nombre ORDER BY monto DESC";
        return lista(sql, desde, hasta);
    }

    /** R6. Clientes que más compran. */
    public List<FilaReporte> clientesFrecuentes(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT CONCAT(u.nombre, ' ', u.apellido) AS periodo, SUM(pe.total) AS monto, COUNT(*) AS cant "
                   + "FROM Pedidos pe JOIN Usuarios u ON u.idUsuario = pe.idUsuario WHERE " + VENTA_VALIDA
                   + "GROUP BY u.idUsuario, u.nombre, u.apellido ORDER BY monto DESC LIMIT 10";
        return lista(sql, desde, hasta);
    }

    /** R7. Pedidos por estado (incluye cancelados). */
    public List<FilaReporte> pedidosPorEstado(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT pe.status AS periodo, SUM(pe.total) AS monto, COUNT(*) AS cant FROM Pedidos pe "
                   + "WHERE pe.fechaPedido >= ? AND pe.fechaPedido < ? GROUP BY pe.status ORDER BY cant DESC";
        return lista(sql, desde, hasta);
    }

    // ---------------------------------------------------------------
    private List<FilaReporte> lista(String sql, LocalDate desde, LocalDate hasta) throws SQLException {
        List<FilaReporte> filas = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int n = contar(sql);
            for (int i = 0; i < n; i += 2) fechas(ps, i + 1, desde, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new FilaReporte(rs.getString("periodo"), rs.getBigDecimal("monto"), rs.getLong("cant")));
                }
            }
        }
        return filas;
    }

    private long escalar(String sql, LocalDate desde, LocalDate hasta) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            fechas(ps, 1, desde, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    /** Asigna el rango [desde, hasta + 1 día) a dos parámetros consecutivos. */
    private void fechas(PreparedStatement ps, int pos, LocalDate desde, LocalDate hasta) throws SQLException {
        ps.setDate(pos, Date.valueOf(desde));
        ps.setDate(pos + 1, Date.valueOf(hasta.plusDays(1)));
    }

    private int contar(String sql) {
        int n = 0;
        for (char c : sql.toCharArray()) if (c == '?') n++;
        return n;
    }
}
