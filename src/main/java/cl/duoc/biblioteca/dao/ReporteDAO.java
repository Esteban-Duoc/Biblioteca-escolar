package cl.duoc.biblioteca.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO extends BaseDAO {
    public List<Object[]> librosMasPrestados(int limite) throws SQLException {
        String sql = "SELECT l.titulo, l.autor, c.nombre AS categoria, l.stock, COUNT(p.id) AS total "
                + "FROM libros l "
                + "LEFT JOIN prestamos p ON p.id_libro = l.id "
                + "LEFT JOIN categorias c ON c.id = l.id_categoria "
                + "GROUP BY l.id, l.titulo, l.autor, c.nombre, l.stock "
                + "ORDER BY total DESC, l.titulo "
                + "LIMIT ?";
        List<Object[]> filas = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                int posicion = 1;
                while (rs.next()) {
                    filas.add(new Object[]{posicion++, rs.getString("titulo"), rs.getString("autor"),
                            rs.getString("categoria"), rs.getInt("total"), rs.getInt("stock")});
                }
            }
        }
        return filas;
    }
}
