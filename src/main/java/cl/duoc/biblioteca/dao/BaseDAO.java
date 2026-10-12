package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseDAO {
    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    protected int ejecutarInsertYObtenerId(PreparedStatement ps) throws SQLException {
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    protected PreparedStatement prepararInsert(String sql) throws SQLException {
        return getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }

    protected String comoLike(String texto) {
        return "%" + (texto == null ? "" : texto.trim()) + "%";
    }
}
