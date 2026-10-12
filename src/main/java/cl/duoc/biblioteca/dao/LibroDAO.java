package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.model.Categoria;
import cl.duoc.biblioteca.model.Libro;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO extends BaseDAO implements CrudDAO<Libro> {
    private static final String SELECT_BASE =
            "SELECT l.id, l.titulo, l.autor, l.isbn, l.editorial, l.stock, l.id_categoria, "
            + "c.nombre AS nombre_categoria "
            + "FROM libros l LEFT JOIN categorias c ON c.id = l.id_categoria ";

    @Override
    public int insertar(Libro l) throws SQLException {
        String sql = "INSERT INTO libros (titulo, autor, isbn, editorial, stock, id_categoria) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = prepararInsert(sql)) {
            llenarParametros(ps, l);
            int id = ejecutarInsertYObtenerId(ps);
            l.setId(id);
            return id;
        }
    }

    @Override
    public Libro buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_BASE + "WHERE l.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Libro buscarPorIdParaActualizar(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_BASE + "WHERE l.id = ? FOR UPDATE")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Libro> listar() throws SQLException {
        return buscar("");
    }

    public List<Libro> buscar(String texto) throws SQLException {
        List<Libro> lista = new ArrayList<>();
        String sql = SELECT_BASE
                + "WHERE l.titulo LIKE ? OR l.autor LIKE ? OR l.isbn LIKE ? OR l.editorial LIKE ? OR c.nombre LIKE ? "
                + "ORDER BY l.titulo";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            String like = comoLike(texto);
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public List<Libro> listarDisponibles() throws SQLException {
        List<Libro> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_BASE + "WHERE l.stock > 0 ORDER BY l.titulo");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public boolean actualizar(Libro l) throws SQLException {
        String sql = "UPDATE libros SET titulo = ?, autor = ?, isbn = ?, editorial = ?, stock = ?, id_categoria = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            llenarParametros(ps, l);
            ps.setInt(7, l.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("DELETE FROM libros WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean descontarStock(int idLibro) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE libros SET stock = stock - 1 WHERE id = ? AND stock > 0")) {
            ps.setInt(1, idLibro);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean aumentarStock(int idLibro) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE libros SET stock = stock + 1 WHERE id = ?")) {
            ps.setInt(1, idLibro);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean existeIsbn(String isbn, int idExcluir) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COUNT(*) FROM libros WHERE isbn = ? AND id <> ?")) {
            ps.setString(1, isbn);
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int contarPrestamos(int idLibro) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COUNT(*) FROM prestamos WHERE id_libro = ?")) {
            ps.setInt(1, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private void llenarParametros(PreparedStatement ps, Libro l) throws SQLException {
        ps.setString(1, l.getTitulo());
        ps.setString(2, l.getAutor());
        ps.setString(3, l.getIsbn());
        ps.setString(4, l.getEditorial());
        ps.setInt(5, l.getStock());
        if (l.getCategoria() != null) {
            ps.setInt(6, l.getCategoria().getId());
        } else {
            ps.setNull(6, Types.INTEGER);
        }
    }

    private Libro mapear(ResultSet rs) throws SQLException {
        Categoria categoria = null;
        int idCategoria = rs.getInt("id_categoria");
        if (!rs.wasNull()) {
            categoria = new Categoria(idCategoria, rs.getString("nombre_categoria"));
        }
        return new Libro(rs.getInt("id"), rs.getString("titulo"), rs.getString("autor"),
                rs.getString("isbn"), rs.getString("editorial"), rs.getInt("stock"), categoria);
    }
}
