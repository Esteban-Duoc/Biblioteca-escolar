package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.model.Categoria;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.model.Prestamo;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO extends BaseDAO implements CrudDAO<Prestamo> {
    private static final String SELECT_BASE =
            "SELECT p.id, p.fecha_prestamo, p.fecha_devolucion, p.devuelto, "
            + " e.id AS e_id, e.nombre AS e_nombre, e.rut AS e_rut, e.curso AS e_curso, e.correo AS e_correo, "
            + " l.id AS l_id, l.titulo AS l_titulo, l.autor AS l_autor, l.isbn AS l_isbn, "
            + " l.editorial AS l_editorial, l.stock AS l_stock, l.id_categoria AS l_id_categoria, "
            + " (SELECT COALESCE(MAX(h.dias_atraso), 0) FROM historial_prestamos h "
            + "   WHERE h.id_prestamo = p.id AND h.accion = 'DEVOLUCION_ATRASADA') AS dias_atraso_reg "
            + "FROM prestamos p "
            + "JOIN estudiantes e ON e.id = p.id_estudiante "
            + "JOIN libros l ON l.id = p.id_libro ";

    @Override
    public int insertar(Prestamo p) throws SQLException {
        String sql = "INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = prepararInsert(sql)) {
            ps.setInt(1, p.getEstudiante().getId());
            ps.setInt(2, p.getLibro().getId());
            ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
            ps.setDate(4, Date.valueOf(p.getFechaVencimiento()));
            ps.setBoolean(5, p.isDevuelto());
            int id = ejecutarInsertYObtenerId(ps);
            p.setId(id);
            return id;
        }
    }

    @Override
    public Prestamo buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_BASE + "WHERE p.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Prestamo> listar() throws SQLException {
        return listarConFiltro("ORDER BY p.fecha_prestamo DESC, p.id DESC", null);
    }

    public List<Prestamo> listarActivos() throws SQLException {
        return listarConFiltro("WHERE p.devuelto = FALSE ORDER BY p.fecha_devolucion, p.id", null);
    }

    public List<Prestamo> listarPorEstudiante(int idEstudiante) throws SQLException {
        return listarConFiltro("WHERE p.id_estudiante = ? ORDER BY p.fecha_prestamo DESC, p.id DESC", idEstudiante);
    }

    @Override
    public boolean actualizar(Prestamo p) throws SQLException {
        String sql = "UPDATE prestamos SET id_estudiante = ?, id_libro = ?, fecha_prestamo = ?, "
                + "fecha_devolucion = ?, devuelto = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, p.getEstudiante().getId());
            ps.setInt(2, p.getLibro().getId());
            ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
            ps.setDate(4, Date.valueOf(p.getFechaVencimiento()));
            ps.setBoolean(5, p.isDevuelto());
            ps.setInt(6, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("DELETE FROM prestamos WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean marcarDevuelto(int idPrestamo) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE prestamos SET devuelto = TRUE WHERE id = ? AND devuelto = FALSE")) {
            ps.setInt(1, idPrestamo);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean tienePrestamoActivo(int idEstudiante, int idLibro) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COUNT(*) FROM prestamos WHERE id_estudiante = ? AND id_libro = ? AND devuelto = FALSE")) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int contarActivosDeEstudiante(int idEstudiante) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COUNT(*) FROM prestamos WHERE id_estudiante = ? AND devuelto = FALSE")) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private List<Prestamo> listarConFiltro(String filtroYOrden, Integer parametro) throws SQLException {
        List<Prestamo> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_BASE + filtroYOrden)) {
            if (parametro != null) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private Prestamo mapear(ResultSet rs) throws SQLException {
        Estudiante e = new Estudiante(rs.getInt("e_id"), rs.getString("e_nombre"), rs.getString("e_rut"),
                rs.getString("e_curso"), rs.getString("e_correo"));
        Categoria categoria = new Categoria(rs.getInt("l_id_categoria"), null);
        Libro l = new Libro(rs.getInt("l_id"), rs.getString("l_titulo"), rs.getString("l_autor"),
                rs.getString("l_isbn"), rs.getString("l_editorial"), rs.getInt("l_stock"), categoria);

        Prestamo p = new Prestamo();
        p.setId(rs.getInt("id"));
        p.setEstudiante(e);
        p.setLibro(l);
        p.setFechaPrestamo(rs.getDate("fecha_prestamo").toLocalDate());
        Date venc = rs.getDate("fecha_devolucion");
        p.setFechaVencimiento(venc == null ? null : venc.toLocalDate());
        p.setDevuelto(rs.getBoolean("devuelto"));
        p.setDiasAtrasoRegistrados(rs.getInt("dias_atraso_reg"));
        return p;
    }
}
