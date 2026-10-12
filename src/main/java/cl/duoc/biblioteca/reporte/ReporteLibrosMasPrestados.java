package cl.duoc.biblioteca.reporte;

import cl.duoc.biblioteca.dao.ReporteDAO;

import java.sql.SQLException;
import java.util.List;

public class ReporteLibrosMasPrestados implements Reporte {
    private static final int TOP = 10;
    private final ReporteDAO reporteDAO = new ReporteDAO();

    @Override
    public String getTitulo() {
        return "Libros más prestados";
    }

    @Override
    public String getDescripcion() {
        return "Top " + TOP + " de libros ordenados por la cantidad total de préstamos registrados.";
    }

    @Override
    public String[] getColumnas() {
        return new String[]{"#", "Título", "Autor", "Categoría", "Veces prestado", "Stock disponible"};
    }

    @Override
    public List<Object[]> generar() throws SQLException {
        return reporteDAO.librosMasPrestados(TOP);
    }
}
