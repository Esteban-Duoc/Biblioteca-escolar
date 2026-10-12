package cl.duoc.biblioteca.dao;

import java.sql.SQLException;
import java.util.List;

public interface CrudDAO<T> {
    int insertar(T entidad) throws SQLException;

    T buscarPorId(int id) throws SQLException;

    List<T> listar() throws SQLException;

    boolean actualizar(T entidad) throws SQLException;

    boolean eliminar(int id) throws SQLException;
}
