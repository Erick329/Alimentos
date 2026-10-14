package com.lacosecha.dao;

import com.lacosecha.modelo.DetallePedido;
import com.lacosecha.modelo.Pedido;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de Pedidos, DetallePedido y Pagos. */
public class PedidoDAO {

    /** Pedido + cliente + su último pago + número de artículos. */
    private static final String SELECT_BASE =
            "SELECT pe.*, CONCAT(u.nombre, ' ', u.apellido) AS cliente, u.email, u.nit, "
          + "pg.metodoPago, pg.status AS statusPago, pg.numeroReferencia, "
          + "(SELECT COALESCE(SUM(d.cantidad), 0) FROM DetallePedido d WHERE d.idPedido = pe.idPedido) AS articulos "
          + "FROM Pedidos pe JOIN Usuarios u ON u.idUsuario = pe.idUsuario "
          + "LEFT JOIN Pagos pg ON pg.idPago = (SELECT MAX(p2.idPago) FROM Pagos p2 WHERE p2.idPedido = pe.idPedido) ";

    /** Inserta el pedido y su detalle usando la conexión de la transacción. Devuelve el id generado. */
    public int insertar(Connection cn, Pedido p) throws SQLException {
        String sql = "INSERT INTO Pedidos (total, direccion, costoEnvio, status, subTotal, iva, idUsuario) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        int idPedido;
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, p.getTotal());
            ps.setString(2, p.getDireccionEnvio());
            ps.setBigDecimal(3, p.getCostoEnvio());
            ps.setString(4, p.getEstado());
            ps.setBigDecimal(5, p.getSubtotal());
            ps.setBigDecimal(6, p.getIva());
            ps.setInt(7, p.getIdUsuario());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                idPedido = rs.getInt(1);
            }
        }
        String sqlDet = "INSERT INTO DetallePedido (cantidad, precioUnitario, subTotal, idProducto, idPedido) "
                      + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sqlDet)) {
            for (DetallePedido d : p.getDetalles()) {
                ps.setInt(1, d.getCantidad());
                ps.setBigDecimal(2, d.getPrecioUnitario());
                ps.setBigDecimal(3, d.getSubtotal());
                ps.setInt(4, d.getIdProducto());
                ps.setInt(5, idPedido);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        return idPedido;
    }

    public void insertarPago(Connection cn, int idPedido, String metodo, BigDecimal monto,
                             String status, String referencia) throws SQLException {
        String sql = "INSERT INTO Pagos (metodoPago, monto, status, numeroReferencia, idPedido) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, metodo);
            ps.setBigDecimal(2, monto);
            ps.setString(3, status);
            ps.setString(4, referencia);
            ps.setInt(5, idPedido);
            ps.executeUpdate();
        }
    }

    public void actualizarEstado(Connection cn, int idPedido, String estado) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE Pedidos SET status = ? WHERE idPedido = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, idPedido);
            ps.executeUpdate();
        }
    }

    /** Actualiza el estado del último pago del pedido. */
    public void actualizarEstadoPago(Connection cn, int idPedido, String estado) throws SQLException {
        String sql = "UPDATE Pagos SET status = ? WHERE idPago = "
                   + "(SELECT idPago FROM (SELECT MAX(idPago) AS idPago FROM Pagos WHERE idPedido = ?) t)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idPedido);
            ps.executeUpdate();
        }
    }

    public List<Pedido> listarPorUsuario(int idUsuario) throws SQLException {
        return consultar(SELECT_BASE + "WHERE pe.idUsuario = ? ORDER BY pe.fechaPedido DESC", idUsuario);
    }

    /** Lista pedidos; estado null = todos. */
    public List<Pedido> listar(String estado, int limite) throws SQLException {
        String sql = SELECT_BASE + (estado == null ? "" : "WHERE pe.status = ? ")
                   + "ORDER BY pe.fechaPedido DESC LIMIT ?";
        return estado == null ? consultar(sql, limite) : consultar(sql, estado, limite);
    }

    /** Pedidos realizados entre dos fechas (incluye ambas). */
    public List<Pedido> listarPorFechas(java.time.LocalDate desde, java.time.LocalDate hasta) throws SQLException {
        return consultar(SELECT_BASE + "WHERE pe.fechaPedido >= ? AND pe.fechaPedido < ? ORDER BY pe.fechaPedido DESC",
                java.sql.Date.valueOf(desde), java.sql.Date.valueOf(hasta.plusDays(1)));
    }

    /** Busca un pedido con su detalle. */
    public Pedido buscar(int idPedido) throws SQLException {
        List<Pedido> r = consultar(SELECT_BASE + "WHERE pe.idPedido = ?", idPedido);
        if (r.isEmpty()) return null;
        Pedido p = r.get(0);
        String sql = "SELECT d.*, pr.nombre, pr.codigo FROM DetallePedido d "
                   + "JOIN Productos pr ON pr.idProducto = d.idProducto WHERE d.idPedido = ?";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetallePedido d = new DetallePedido();
                    d.setIdDetalle(rs.getInt("idDetallePedido"));
                    d.setIdPedido(idPedido);
                    d.setIdProducto(rs.getInt("idProducto"));
                    d.setProductoNombre(rs.getString("nombre"));
                    d.setProductoCodigo(rs.getString("codigo"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getBigDecimal("precioUnitario"));
                    d.setSubtotal(rs.getBigDecimal("subTotal"));
                    p.getDetalles().add(d);
                }
            }
        }
        return p;
    }

    private List<Pedido> consultar(String sql, Object... params) throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setIdPedido(rs.getInt("idPedido"));
        p.setIdUsuario(rs.getInt("idUsuario"));
        p.setClienteNombre(rs.getString("cliente"));
        p.setClienteEmail(rs.getString("email"));
        p.setClienteNit(rs.getString("nit"));
        p.setFechaPedido(rs.getTimestamp("fechaPedido"));
        p.setEstado(rs.getString("status"));
        p.setSubtotal(rs.getBigDecimal("subTotal"));
        p.setIva(rs.getBigDecimal("iva"));
        p.setCostoEnvio(rs.getBigDecimal("costoEnvio"));
        p.setTotal(rs.getBigDecimal("total"));
        p.setDireccionEnvio(rs.getString("direccion"));
        p.setMetodoPago(rs.getString("metodoPago"));
        p.setEstadoPago(rs.getString("statusPago"));
        p.setReferenciaPago(rs.getString("numeroReferencia"));
        p.setTotalArticulos(rs.getInt("articulos"));
        return p;
    }
}
