package cl.duoc.biblioteca.reporte;

import java.sql.SQLException;
import java.util.List;

public interface Reporte {
    String getTitulo();

    String getDescripcion();

    String[] getColumnas();

    List<Object[]> generar() throws SQLException;
}
