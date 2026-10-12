package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.model.Estudiante;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO extends BaseDAO implements CrudDAO<Estudiante> {
    private static final String SELECT_BASE = "SELECT id, nombre, rut, curso, correo FROM estudiantes ";

    @Override
    public int insertar(Estudiante e) throws SQLException {
        String sql = "INSERT INTO estudiantes (nombre, rut, curso, correo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = prepararInsert(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getRut());
            ps.setString(3, e.getCurso());
            ps.setString(4, e.getCorreo());
            int id = ejecutarInsertYObtenerId(ps);
            e.setId(id);
            return id;
        }
    }

    @Override
    public Estudiante buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_BASE + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Estudiante buscarPorRut(String rut) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_BASE + "WHERE rut = ?")) {
            ps.setString(1, rut);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Estudiante> listar() throws SQLException {
        return buscar("");
    }

    public List<Estudiante> buscar(String texto) throws SQLException {
        List<Estudiante> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE nombre LIKE ? OR rut LIKE ? OR curso LIKE ? OR correo LIKE ? ORDER BY nombre";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            String like = comoLike(texto);
            for (int i = 1; i <= 4; i++) {
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

    @Override
    public boolean actualizar(Estudiante e) throws SQLException {
        String sql = "UPDATE estudiantes SET nombre = ?, rut = ?, curso = ?, correo = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getRut());
            ps.setString(3, e.getCurso());
            ps.setString(4, e.getCorreo());
            ps.setInt(5, e.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("DELETE FROM estudiantes WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean existeRut(String rut, int idExcluir) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COUNT(*) FROM estudiantes WHERE rut = ? AND id <> ?")) {
            ps.setString(1, rut);
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int contarPrestamos(int idEstudiante) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COUNT(*) FROM prestamos WHERE id_estudiante = ?")) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Estudiante mapear(ResultSet rs) throws SQLException {
        return new Estudiante(rs.getInt("id"), rs.getString("nombre"), rs.getString("rut"),
                rs.getString("curso"), rs.getString("correo"));
    }
}
