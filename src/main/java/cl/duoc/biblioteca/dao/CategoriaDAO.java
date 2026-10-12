package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.model.Categoria;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO extends BaseDAO implements CrudDAO<Categoria> {
    @Override
    public int insertar(Categoria c) throws SQLException {
        try (PreparedStatement ps = prepararInsert("INSERT INTO categorias (nombre) VALUES (?)")) {
            ps.setString(1, c.getNombre());
            int id = ejecutarInsertYObtenerId(ps);
            c.setId(id);
            return id;
        }
    }

    @Override
    public Categoria buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT id, nombre FROM categorias WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Categoria(rs.getInt("id"), rs.getString("nombre")) : null;
            }
        }
    }

    @Override
    public List<Categoria> listar() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT id, nombre FROM categorias ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Categoria(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return lista;
    }

    @Override
    public boolean actualizar(Categoria c) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE categorias SET nombre = ? WHERE id = ?")) {
            ps.setString(1, c.getNombre());
            ps.setInt(2, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("DELETE FROM categorias WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int contarLibros(int idCategoria) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COUNT(*) FROM libros WHERE id_categoria = ?")) {
            ps.setInt(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
