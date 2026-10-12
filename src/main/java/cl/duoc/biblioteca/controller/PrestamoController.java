package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.HistorialDAO;
import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.dao.PrestamoDAO;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.exception.ValidacionException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.model.Prestamo;
import cl.duoc.biblioteca.service.PrestamoService;
import cl.duoc.biblioteca.service.PruebaConcurrencia;
import cl.duoc.biblioteca.service.RegistroPrestamoTask;
import cl.duoc.biblioteca.service.ResultadoListener;
import cl.duoc.biblioteca.util.ErroresBD;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PrestamoController {
    private static final AtomicInteger CONTADOR_HILOS = new AtomicInteger();

    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final LibroDAO libroDAO = new LibroDAO();
    private final HistorialDAO historialDAO = new HistorialDAO();
    private final PrestamoService prestamoService = PrestamoService.getInstance();

    public void registrarPrestamoEnSegundoPlano(Estudiante estudiante, Libro libro, int dias,
                                                ResultadoListener<Prestamo> listener) throws ValidacionException {
        if (estudiante == null) {
            throw new ValidacionException("Selecciona un estudiante.");
        }
        if (libro == null) {
            throw new ValidacionException("Selecciona un libro.");
        }
        if (dias < PrestamoService.MIN_DIAS || dias > PrestamoService.MAX_DIAS) {
            throw new ValidacionException("Los días de préstamo deben estar entre "
                    + PrestamoService.MIN_DIAS + " y " + PrestamoService.MAX_DIAS + ".");
        }
        RegistroPrestamoTask tarea = new RegistroPrestamoTask(estudiante.getId(), libro.getId(), dias,
                RegistroPrestamoTask.DEMORA_SIMULADA_MS, listener);
        Thread hilo = new Thread(tarea, "Hilo-Prestamo-" + CONTADOR_HILOS.incrementAndGet());
        hilo.start();
    }

    public long registrarDevolucion(Prestamo prestamo) throws BibliotecaException {
        if (prestamo == null) {
            throw new ValidacionException("Selecciona un préstamo de la tabla.");
        }
        return prestamoService.registrarDevolucion(prestamo.getId());
    }

    public long registrarConstanciaAtraso(Prestamo prestamo) throws BibliotecaException {
        if (prestamo == null) {
            throw new ValidacionException("Selecciona un préstamo de la tabla.");
        }
        return prestamoService.registrarConstanciaAtraso(prestamo.getId());
    }

    public LocalDate calcularVencimiento(int dias) {
        return Prestamo.calcularVencimiento(LocalDate.now(), dias);
    }

    public List<Prestamo> listarActivos() throws BibliotecaException {
        try {
            return prestamoDAO.listarActivos();
        } catch (SQLException e) {
            throw ErroresBD.traducir("listar préstamos activos", e);
        }
    }

    public List<Prestamo> listarTodos() throws BibliotecaException {
        try {
            return prestamoDAO.listar();
        } catch (SQLException e) {
            throw ErroresBD.traducir("listar préstamos", e);
        }
    }

    public List<Prestamo> listarDeEstudiante(Estudiante estudiante) throws BibliotecaException {
        try {
            return prestamoDAO.listarPorEstudiante(estudiante.getId());
        } catch (SQLException e) {
            throw ErroresBD.traducir("listar tus préstamos", e);
        }
    }

    public List<Object[]> movimientosDeEstudiante(Estudiante estudiante) throws BibliotecaException {
        try {
            return historialDAO.listarPorEstudiante(estudiante.getId());
        } catch (SQLException e) {
            throw ErroresBD.traducir("leer el historial", e);
        }
    }

    public List<Estudiante> listarEstudiantes() throws BibliotecaException {
        try {
            return estudianteDAO.listar();
        } catch (SQLException e) {
            throw ErroresBD.traducir("listar estudiantes", e);
        }
    }

    public List<Libro> listarLibros() throws BibliotecaException {
        try {
            return libroDAO.listar();
        } catch (SQLException e) {
            throw ErroresBD.traducir("listar libros", e);
        }
    }

    public List<Libro> listarLibrosDisponibles() throws BibliotecaException {
        try {
            return libroDAO.listarDisponibles();
        } catch (SQLException e) {
            throw ErroresBD.traducir("listar libros disponibles", e);
        }
    }

    public List<Estudiante> estudiantesParaPrueba(Libro libro, int cantidad) throws BibliotecaException {
        try {
            List<Estudiante> elegibles = new ArrayList<>();
            for (Estudiante e : estudianteDAO.listar()) {
                if (elegibles.size() == cantidad) {
                    break;
                }
                boolean yaLoTiene = prestamoDAO.tienePrestamoActivo(e.getId(), libro.getId());
                boolean enElMaximo = prestamoDAO.contarActivosDeEstudiante(e.getId()) >= PrestamoService.MAX_PRESTAMOS_ACTIVOS;
                if (!yaLoTiene && !enElMaximo) {
                    elegibles.add(e);
                }
            }
            return elegibles;
        } catch (SQLException e) {
            throw ErroresBD.traducir("preparar la prueba", e);
        }
    }

    public void ejecutarPruebaConcurrencia(Libro libro, List<Estudiante> estudiantes, boolean revertir,
                                           PruebaConcurrencia.Observador observador) throws ValidacionException {
        if (libro == null) {
            throw new ValidacionException("Selecciona un libro.");
        }
        if (estudiantes.size() < 2) {
            throw new ValidacionException("Se necesitan al menos 2 estudiantes disponibles para la prueba.");
        }
        new PruebaConcurrencia().ejecutar(libro, estudiantes, revertir, observador);
    }
}
