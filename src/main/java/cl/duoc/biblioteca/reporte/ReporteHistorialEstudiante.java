package cl.duoc.biblioteca.reporte;

import cl.duoc.biblioteca.dao.PrestamoDAO;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Prestamo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReporteHistorialEstudiante implements Reporte {
    private final Estudiante estudiante;
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();

    public ReporteHistorialEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    @Override
    public String getTitulo() {
        return "Historial por estudiante";
    }

    @Override
    public String getDescripcion() {
        return "Préstamos de " + estudiante.getNombre() + " (" + estudiante.getCurso() + "), del más reciente al más antiguo.";
    }

    @Override
    public String[] getColumnas() {
        return new String[]{"N° préstamo", "Libro", "Autor", "Fecha préstamo", "Fecha vencimiento", "Estado"};
    }

    @Override
    public List<Object[]> generar() throws SQLException {
        LocalDate hoy = LocalDate.now();
        List<Object[]> filas = new ArrayList<>();
        for (Prestamo p : prestamoDAO.listarPorEstudiante(estudiante.getId())) {
            filas.add(new Object[]{p.getId(), p.getLibro().getTitulo(), p.getLibro().getAutor(),
                    p.getFechaPrestamo(), p.getFechaVencimiento(), p.getDescripcionEstado(hoy)});
        }
        return filas;
    }
}
