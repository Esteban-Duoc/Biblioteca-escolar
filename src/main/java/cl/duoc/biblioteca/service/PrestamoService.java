package cl.duoc.biblioteca.service;

import cl.duoc.biblioteca.config.DatabaseConnection;
import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.HistorialDAO;
import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.dao.PrestamoDAO;
import cl.duoc.biblioteca.exception.PrestamoException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.model.Prestamo;

import java.time.LocalDate;

public final class PrestamoService {
    public static final int MAX_PRESTAMOS_ACTIVOS = 3;
    public static final int MIN_DIAS = 1;
    public static final int MAX_DIAS = 30;

    private static PrestamoService instance;

    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final LibroDAO libroDAO = new LibroDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final HistorialDAO historialDAO = new HistorialDAO();

    private PrestamoService() {
    }

    public static synchronized PrestamoService getInstance() {
        if (instance == null) {
            instance = new PrestamoService();
        }
        return instance;
    }

    public synchronized Prestamo registrarPrestamo(int idEstudiante, int idLibro, int dias) throws PrestamoException {
        if (dias < MIN_DIAS || dias > MAX_DIAS) {
            throw new PrestamoException("Los días de préstamo deben estar entre " + MIN_DIAS + " y " + MAX_DIAS + ".");
        }
        try {
            return DatabaseConnection.getInstance().ejecutarEnTransaccion(con -> {
                Estudiante estudiante = estudianteDAO.buscarPorId(idEstudiante);
                if (estudiante == null) {
                    throw new PrestamoException("El estudiante seleccionado no existe.");
                }
                Libro libro = libroDAO.buscarPorIdParaActualizar(idLibro);
                if (libro == null) {
                    throw new PrestamoException("El libro seleccionado no existe.");
                }
                if (!libro.hayStock()) {
                    throw new PrestamoException("No hay copias disponibles de \"" + libro.getTitulo() + "\".");
                }
                if (prestamoDAO.tienePrestamoActivo(idEstudiante, idLibro)) {
                    throw new PrestamoException(estudiante.getNombre() + " ya tiene prestado \"" + libro.getTitulo() + "\".");
                }
                if (prestamoDAO.contarActivosDeEstudiante(idEstudiante) >= MAX_PRESTAMOS_ACTIVOS) {
                    throw new PrestamoException(estudiante.getNombre() + " ya tiene " + MAX_PRESTAMOS_ACTIVOS
                            + " libros sin devolver (máximo permitido).");
                }

                Prestamo prestamo = new Prestamo(estudiante, libro, LocalDate.now(), dias);
                prestamoDAO.insertar(prestamo);

                if (!libroDAO.descontarStock(idLibro)) {
                    throw new PrestamoException("El stock de \"" + libro.getTitulo() + "\" se agotó.");
                }
                libro.setStock(libro.getStock() - 1);

                historialDAO.registrar(prestamo.getId(), idEstudiante, idLibro, HistorialDAO.PRESTAMO, 0,
                        "Préstamo por " + dias + " días. Vence el " + prestamo.getFechaVencimiento());
                return prestamo;
            });
        } catch (PrestamoException e) {
            throw e;
        } catch (Exception e) {
            throw new PrestamoException("Error de base de datos al registrar el préstamo: " + e.getMessage(), e);
        }
    }

    public synchronized long registrarDevolucion(int idPrestamo) throws PrestamoException {
        try {
            return DatabaseConnection.getInstance().ejecutarEnTransaccion(con -> {
                Prestamo prestamo = prestamoDAO.buscarPorId(idPrestamo);
                if (prestamo == null) {
                    throw new PrestamoException("El préstamo no existe.");
                }
                if (prestamo.isDevuelto() || !prestamoDAO.marcarDevuelto(idPrestamo)) {
                    throw new PrestamoException("Este préstamo ya fue devuelto.");
                }
                libroDAO.aumentarStock(prestamo.getLibro().getId());

                LocalDate hoy = LocalDate.now();
                long diasAtraso = prestamo.diasAtraso(hoy);
                String accion = diasAtraso > 0 ? HistorialDAO.DEVOLUCION_ATRASADA : HistorialDAO.DEVOLUCION;
                String detalle = diasAtraso > 0
                        ? "Devuelto el " + hoy + " con " + diasAtraso + " días de atraso (vencía el " + prestamo.getFechaVencimiento() + ")"
                        : "Devuelto a tiempo el " + hoy;
                historialDAO.registrar(idPrestamo, prestamo.getEstudiante().getId(), prestamo.getLibro().getId(),
                        accion, diasAtraso, detalle);
                return diasAtraso;
            });
        } catch (PrestamoException e) {
            throw e;
        } catch (Exception e) {
            throw new PrestamoException("Error de base de datos al registrar la devolución: " + e.getMessage(), e);
        }
    }

    public synchronized long registrarConstanciaAtraso(int idPrestamo) throws PrestamoException {
        try {
            return DatabaseConnection.getInstance().ejecutarEnTransaccion(con -> {
                Prestamo prestamo = prestamoDAO.buscarPorId(idPrestamo);
                if (prestamo == null) {
                    throw new PrestamoException("El préstamo no existe.");
                }
                LocalDate hoy = LocalDate.now();
                if (!prestamo.estaAtrasado(hoy)) {
                    throw new PrestamoException("Este préstamo no está atrasado.");
                }
                if (historialDAO.existeConstanciaHoy(idPrestamo)) {
                    throw new PrestamoException("Ya se dejó constancia del atraso de este préstamo hoy.");
                }
                long dias = prestamo.diasAtraso(hoy);
                historialDAO.registrar(idPrestamo, prestamo.getEstudiante().getId(), prestamo.getLibro().getId(),
                        HistorialDAO.CONSTANCIA_ATRASO, dias,
                        "Constancia: " + dias + " días de atraso al " + hoy + " (vencía el " + prestamo.getFechaVencimiento() + ")");
                return dias;
            });
        } catch (PrestamoException e) {
            throw e;
        } catch (Exception e) {
            throw new PrestamoException("Error de base de datos al registrar la constancia: " + e.getMessage(), e);
        }
    }

    public synchronized void revertirPrestamo(int idPrestamo) throws PrestamoException {
        try {
            DatabaseConnection.getInstance().ejecutarEnTransaccion(con -> {
                Prestamo prestamo = prestamoDAO.buscarPorId(idPrestamo);
                if (prestamo == null) {
                    return null;
                }
                historialDAO.eliminarPorPrestamo(idPrestamo);
                prestamoDAO.eliminar(idPrestamo);
                if (!prestamo.isDevuelto()) {
                    libroDAO.aumentarStock(prestamo.getLibro().getId());
                }
                return null;
            });
        } catch (Exception e) {
            throw new PrestamoException("No se pudo revertir el préstamo " + idPrestamo + ": " + e.getMessage(), e);
        }
    }
}
