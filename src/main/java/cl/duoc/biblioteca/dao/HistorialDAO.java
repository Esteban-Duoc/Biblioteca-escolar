package cl.duoc.biblioteca.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class HistorialDAO extends BaseDAO {
    public static final String PRESTAMO = "PRESTAMO";
    public static final String DEVOLUCION = "DEVOLUCION";
    public static final String DEVOLUCION_ATRASADA = "DEVOLUCION_ATRASADA";
    public static final String CONSTANCIA_ATRASO = "CONSTANCIA_ATRASO";

    public void crearTablaSiNoExiste() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS historial_prestamos ("
                + " id INT PRIMARY KEY AUTO_INCREMENT,"
                + " id_prestamo INT,"
                + " id_estudiante INT,"
                + " id_libro INT,"
                + " accion VARCHAR(30) NOT NULL,"
                + " fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + " dias_atraso INT DEFAULT 0,"
                + " detalle VARCHAR(255),"
                + " FOREIGN KEY (id_prestamo) REFERENCES prestamos(id) ON DELETE CASCADE,"
                + " FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id) ON DELETE CASCADE,"
                + " FOREIGN KEY (id_libro) REFERENCES libros(id) ON DELETE CASCADE)";
        try (Statement st = getConnection().createStatement()) {
            st.executeUpdate(sql);
        }
    }

    public void registrar(int idPrestamo, int idEstudiante, int idLibro, String accion,
                          long diasAtraso, String detalle) throws SQLException {
        String sql = "INSERT INTO historial_prestamos (id_prestamo, id_estudiante, id_libro, accion, dias_atraso, detalle) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            ps.setInt(2, idEstudiante);
            ps.setInt(3, idLibro);
            ps.setString(4, accion);
            ps.setLong(5, diasAtraso);
            ps.setString(6, detalle);
            ps.executeUpdate();
        }
    }

    public boolean existeConstanciaHoy(int idPrestamo) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COUNT(*) FROM historial_prestamos WHERE id_prestamo = ? AND accion = ? AND DATE(fecha) = CURDATE()")) {
            ps.setInt(1, idPrestamo);
            ps.setString(2, CONSTANCIA_ATRASO);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public List<Object[]> listarPorEstudiante(int idEstudiante) throws SQLException {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT h.fecha, h.accion, l.titulo, h.dias_atraso, h.detalle "
                + "FROM historial_prestamos h LEFT JOIN libros l ON l.id = h.id_libro "
                + "WHERE h.id_estudiante = ? ORDER BY h.fecha DESC, h.id DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp fecha = rs.getTimestamp("fecha");
                    filas.add(new Object[]{
                            fecha == null ? "" : fecha.toLocalDateTime().toString().replace('T', ' '),
                            rs.getString("accion"), rs.getString("titulo"),
                            rs.getInt("dias_atraso"), rs.getString("detalle")});
                }
            }
        }
        return filas;
    }

    public void eliminarPorPrestamo(int idPrestamo) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "DELETE FROM historial_prestamos WHERE id_prestamo = ?")) {
            ps.setInt(1, idPrestamo);
            ps.executeUpdate();
        }
    }
}
