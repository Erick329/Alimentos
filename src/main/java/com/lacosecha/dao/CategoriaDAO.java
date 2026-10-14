package com.lacosecha.dao;

import com.lacosecha.modelo.Categoria;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD de la tabla Categorias. */
public class CategoriaDAO {

    public List<Categoria> listar(boolean soloActivas) throws SQLException {
        String sql = "SELECT c.*, (SELECT COUNT(*) FROM Productos p WHERE p.idCategoria = c.idCategoria) AS total "
                   + "FROM Categorias c " + (soloActivas ? "WHERE c.status = 'ACTIVO' " : "") + "ORDER BY c.nombre";
        List<Categoria> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categoria c = new Categoria();
                c.setIdCategoria(rs.getInt("idCategoria"));
                c.setNombre(rs.getString("nombre"));
                c.setDescripcion(rs.getString("descripcion"));
                c.setStatus(rs.getString("status"));
                c.setTotalProductos(rs.getInt("total"));
                lista.add(c);
            }
        }
        return lista;
    }

    public Categoria buscar(int id) throws SQLException {
        for (Categoria c : listar(false)) if (c.getIdCategoria() == id) return c;
        return null;
    }

    /** Inserta si el id es 0; de lo contrario actualiza. */
    public void guardar(Categoria c) throws SQLException {
        boolean nuevo = c.getIdCategoria() == 0;
        String sql = nuevo
                ? "INSERT INTO Categorias (nombre, descripcion, status) VALUES (?, ?, ?)"
                : "UPDATE Categorias SET nombre = ?, descripcion = ?, status = ? WHERE idCategoria = ?";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDescripcion());
            ps.setString(3, c.getStatus());
            if (!nuevo) ps.setInt(4, c.getIdCategoria());
            ps.executeUpdate();
        }
    }
}
