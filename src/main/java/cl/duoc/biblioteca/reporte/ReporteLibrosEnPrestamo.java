package cl.duoc.biblioteca.reporte;

import cl.duoc.biblioteca.dao.PrestamoDAO;
import cl.duoc.biblioteca.model.Prestamo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReporteLibrosEnPrestamo implements Reporte {
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();

    @Override
    public String getTitulo() {
        return "Libros actualmente en préstamo";
    }

    @Override
    public String getDescripcion() {
        return "Préstamos sin devolver, ordenados por fecha de vencimiento (primero los más urgentes).";
    }

    @Override
    public String[] getColumnas() {
        return new String[]{"N° préstamo", "Libro", "Estudiante", "Curso", "Fecha préstamo", "Vence", "Estado"};
    }

    @Override
    public List<Object[]> generar() throws SQLException {
        LocalDate hoy = LocalDate.now();
        List<Object[]> filas = new ArrayList<>();
        for (Prestamo p : prestamoDAO.listarActivos()) {
            filas.add(new Object[]{p.getId(), p.getLibro().getTitulo(), p.getEstudiante().getNombre(),
                    p.getEstudiante().getCurso(), p.getFechaPrestamo(), p.getFechaVencimiento(),
                    p.getDescripcionEstado(hoy)});
        }
        return filas;
    }
}
