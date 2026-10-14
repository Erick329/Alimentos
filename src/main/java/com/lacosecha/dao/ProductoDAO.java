package com.lacosecha.dao;

import com.lacosecha.modelo.MovimientoInventario;
import com.lacosecha.modelo.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de Productos y Movimientos. */
public class ProductoDAO {

    /** Incluye la categoría y la fecha del lote con existencias que vence primero. */
    private static final String SELECT_BASE =
            "SELECT p.*, c.nombre AS categoria, "
          + "(SELECT MIN(l.fechaVencimiento) FROM Lotes l WHERE l.idProducto = p.idProducto "
          + "   AND l.status = 'ACTIVO' AND l.cantidad > 0) AS proximoVencimiento "
          + "FROM Productos p JOIN Categorias c ON c.idCategoria = p.idCategoria ";

    /**
     * Lista productos con filtros opcionales.
     * @param idCategoria 0 = todas
     * @param texto       búsqueda por nombre o código (null = sin filtro)
     * @param soloActivos true para ventas (productos y categorías ACTIVOS)
     */
    public List<Producto> listar(int idCategoria, String texto, boolean soloActivos) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();
        if (soloActivos) sql.append("AND p.status = 'ACTIVO' AND c.status = 'ACTIVO' ");
        if (idCategoria > 0) {
            sql.append("AND p.idCategoria = ? ");
            params.add(idCategoria);
        }
        if (texto != null && !texto.isBlank()) {
            sql.append("AND (p.nombre LIKE ? OR p.codigo LIKE ?) ");
            params.add("%" + texto.trim() + "%");
            params.add("%" + texto.trim() + "%");
        }
        sql.append("ORDER BY p.nombre");
        List<Producto> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Producto buscar(int id) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE p.idProducto = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Productos más vendidos (para el menú del cliente). */
    public List<Producto> destacados(int limite) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.status = 'ACTIVO' AND c.status = 'ACTIVO' AND p.stock > 0 "
                + "ORDER BY (SELECT COALESCE(SUM(d.cantidad), 0) FROM DetallePedido d "
                + "WHERE d.idProducto = p.idProducto) DESC, p.nombre LIMIT ?";
        List<Producto> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Producto> bajoStock() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE
                     + "WHERE p.status = 'ACTIVO' AND p.stock <= p.stockMinimo ORDER BY p.stock");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    /**
     * Inserta (id = 0) o actualiza un producto.
     * El stock NO se modifica aquí: se calcula a partir de los lotes (ver LoteDAO / triggers).
     */
    public void guardar(Producto p) throws SQLException {
        boolean nuevo = p.getIdProducto() == 0;
        String sql = nuevo
                ? "INSERT INTO Productos (nombre, descripcion, codigo, precio, stockMinimo, status, idCategoria) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)"
                : "UPDATE Productos SET nombre = ?, descripcion = ?, codigo = ?, precio = ?, stockMinimo = ?, "
                + "status = ?, idCategoria = ? WHERE idProducto = ?";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setString(3, p.getCodigo());
            ps.setBigDecimal(4, p.getPrecio());
            ps.setInt(5, p.getStockMinimo());
            ps.setString(6, p.getStatus());
            ps.setInt(7, p.getIdCategoria());
            if (!nuevo) ps.setInt(8, p.getIdProducto());
            ps.executeUpdate();
            if (nuevo) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) p.setIdProducto(rs.getInt(1));   // id del producto recién creado
                }
            }
        }
    }

    public void cambiarStatus(int idProducto, String status) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement("UPDATE Productos SET status = ? WHERE idProducto = ?")) {
            ps.setString(1, status);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------
    // Métodos que reciben la conexión para participar en una transacción
    // ------------------------------------------------------------------

    /** Lee y BLOQUEA la fila del producto hasta el commit/rollback (evita vender dos veces lo mismo). */
    public Producto bloquear(Connection cn, int idProducto) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE p.idProducto = ? FOR UPDATE")) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Recalcula Productos.stock = suma de lotes ACTIVOS.
     * Hace lo mismo que los triggers de la base de datos; así el stock queda correcto
     * aunque los triggers no existan.
     */
    public void recalcularStock(Connection cn, int idProducto) throws SQLException {
        String sql = "UPDATE Productos SET stock = (SELECT COALESCE(SUM(cantidad), 0) FROM Lotes "
                   + "WHERE idProducto = ? AND status = 'ACTIVO') WHERE idProducto = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        }
    }

    /** Registra una fila en Movimientos (idLote puede ser null). */
    public void registrarMovimiento(Connection cn, String tipo, int cantidad, String motivo,
                                    int idProducto, int idUsuario, Integer idLote) throws SQLException {
        String sql = "INSERT INTO Movimientos (tipo, cantidad, motivo, idProducto, idUsuario, idLote) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, tipo);
            ps.setInt(2, cantidad);
            ps.setString(3, motivo);
            ps.setInt(4, idProducto);
            ps.setInt(5, idUsuario);
            if (idLote == null) ps.setNull(6, Types.INTEGER); else ps.setInt(6, idLote);
            ps.executeUpdate();
        }
    }

    /** Salidas de una venta (para devolverlas a sus lotes si el pedido se cancela). */
    public List<MovimientoInventario> salidasDeVenta(Connection cn, int idPedido) throws SQLException {
        String sql = "SELECT * FROM Movimientos WHERE tipo = 'SALIDA' AND motivo = ?";
        List<MovimientoInventario> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, "Venta pedido #" + idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MovimientoInventario m = new MovimientoInventario();
                    m.setIdProducto(rs.getInt("idProducto"));
                    m.setCantidad(rs.getInt("cantidad"));
                    int idLote = rs.getInt("idLote");
                    m.setIdLote(rs.wasNull() ? null : idLote);
                    lista.add(m);
                }
            }
        }
        return lista;
    }

    public List<MovimientoInventario> ultimosMovimientos(int limite) throws SQLException {
        String sql = "SELECT m.*, p.nombre AS producto, CONCAT(u.nombre, ' ', u.apellido) AS usuario, l.numeroLote "
                   + "FROM Movimientos m JOIN Productos p ON p.idProducto = m.idProducto "
                   + "JOIN Usuarios u ON u.idUsuario = m.idUsuario "
                   + "LEFT JOIN Lotes l ON l.idLote = m.idLote "
                   + "ORDER BY m.fecha DESC, m.idMovimiento DESC LIMIT ?";
        List<MovimientoInventario> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MovimientoInventario m = new MovimientoInventario();
                    m.setIdMovimiento(rs.getInt("idMovimiento"));
                    m.setTipo(rs.getString("tipo"));
                    m.setCantidad(rs.getInt("cantidad"));
                    m.setMotivo(rs.getString("motivo"));
                    m.setFecha(rs.getTimestamp("fecha"));
                    m.setIdProducto(rs.getInt("idProducto"));
                    m.setProductoNombre(rs.getString("producto"));
                    m.setUsuarioNombre(rs.getString("usuario"));
                    m.setNumeroLote(rs.getString("numeroLote"));
                    lista.add(m);
                }
            }
        }
        return lista;
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("idProducto"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setCodigo(rs.getString("codigo"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setStock(rs.getInt("stock"));
        p.setStockMinimo(rs.getInt("stockMinimo"));
        p.setStatus(rs.getString("status"));
        p.setIdCategoria(rs.getInt("idCategoria"));
        p.setCategoriaNombre(rs.getString("categoria"));
        p.setProximoVencimiento(rs.getDate("proximoVencimiento"));
        return p;
    }
}
