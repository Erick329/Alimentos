package com.lacosecha.dao;

import com.lacosecha.modelo.Lote;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de la tabla Lotes. */
public class LoteDAO {

    private static final String SELECT_BASE =
            "SELECT l.*, p.nombre AS producto, p.codigo FROM Lotes l JOIN Productos p ON p.idProducto = l.idProducto ";

    /** Lotes activos de un producto, del que vence primero al último. */
    public List<Lote> listarPorProducto(int idProducto) throws SQLException {
        return consultar(SELECT_BASE + "WHERE l.idProducto = ? AND l.status = 'ACTIVO' "
                + "ORDER BY l.fechaVencimiento, l.idLote", idProducto);
    }

    /** Lotes con existencias que vencen dentro de "dias" días (incluye los ya vencidos). */
    public List<Lote> proximosAVencer(int dias) throws SQLException {
        return consultar(SELECT_BASE + "WHERE l.status = 'ACTIVO' AND l.cantidad > 0 "
                + "AND l.fechaVencimiento <= DATE_ADD(CURDATE(), INTERVAL ? DAY) "
                + "ORDER BY l.fechaVencimiento", dias);
    }

    // ---------------- Métodos para transacciones ----------------

    /**
     * Lotes de un producto disponibles para vender, BLOQUEADOS y ordenados por vencimiento
     * (FEFO: lo primero que vence es lo primero que sale). No incluye lotes vencidos.
     */
    public List<Lote> disponiblesParaVenta(Connection cn, int idProducto) throws SQLException {
        String sql = SELECT_BASE + "WHERE l.idProducto = ? AND l.status = 'ACTIVO' AND l.cantidad > 0 "
                   + "AND l.fechaVencimiento >= CURDATE() ORDER BY l.fechaVencimiento, l.idLote FOR UPDATE";
        return consultar(cn, sql, idProducto);
    }

    public Lote bloquear(Connection cn, int idLote) throws SQLException {
        List<Lote> r = consultar(cn, SELECT_BASE + "WHERE l.idLote = ? FOR UPDATE", idLote);
        return r.isEmpty() ? null : r.get(0);
    }

    /** Crea un lote nuevo y devuelve su id. */
    public int insertar(Connection cn, Lote l) throws SQLException {
        String sql = "INSERT INTO Lotes (numeroLote, cantidad, fechaVencimiento, idProducto) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, l.getNumeroLote());
            ps.setInt(2, l.getCantidad());
            ps.setDate(3, new java.sql.Date(l.getFechaVencimiento().getTime()));
            ps.setInt(4, l.getIdProducto());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Suma (o resta, si es negativo) unidades a un lote. */
    public void sumarCantidad(Connection cn, int idLote, int cambio) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE Lotes SET cantidad = cantidad + ? WHERE idLote = ?")) {
            ps.setInt(1, cambio);
            ps.setInt(2, idLote);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------
    private List<Lote> consultar(String sql, Object param) throws SQLException {
        try (Connection cn = ConexionBD.getConexion()) {
            return consultar(cn, sql, param);
        }
    }

    private List<Lote> consultar(Connection cn, String sql, Object param) throws SQLException {
        List<Lote> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Lote l = new Lote();
                    l.setIdLote(rs.getInt("idLote"));
                    l.setNumeroLote(rs.getString("numeroLote"));
                    l.setCantidad(rs.getInt("cantidad"));
                    l.setFechaVencimiento(rs.getDate("fechaVencimiento"));
                    l.setFechaIngreso(rs.getTimestamp("fechaIngreso"));
                    l.setStatus(rs.getString("status"));
                    l.setIdProducto(rs.getInt("idProducto"));
                    l.setProductoNombre(rs.getString("producto"));
                    l.setProductoCodigo(rs.getString("codigo"));
                    lista.add(l);
                }
            }
        }
        return lista;
    }
}
