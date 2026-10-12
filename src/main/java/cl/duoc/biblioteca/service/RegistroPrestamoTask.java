package cl.duoc.biblioteca.service;

import cl.duoc.biblioteca.exception.PrestamoException;
import cl.duoc.biblioteca.model.Prestamo;

import javax.swing.SwingUtilities;

public class RegistroPrestamoTask implements Runnable {
    public static final int DEMORA_SIMULADA_MS = 1500;

    private final int idEstudiante;
    private final int idLibro;
    private final int dias;
    private final int demoraMs;
    private final ResultadoListener<Prestamo> listener;

    public RegistroPrestamoTask(int idEstudiante, int idLibro, int dias, int demoraMs,
                                ResultadoListener<Prestamo> listener) {
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.dias = dias;
        this.demoraMs = demoraMs;
        this.listener = listener;
    }

    @Override
    public void run() {
        String hilo = Thread.currentThread().getName();
        System.out.println("[" + hilo + "] Iniciando registro de préstamo (estudiante " + idEstudiante
                + ", libro " + idLibro + ")");
        try {
            if (demoraMs > 0) {
                Thread.sleep(demoraMs);
            }
            Prestamo prestamo = PrestamoService.getInstance().registrarPrestamo(idEstudiante, idLibro, dias);
            System.out.println("[" + hilo + "] Préstamo #" + prestamo.getId() + " registrado.");
            SwingUtilities.invokeLater(() -> listener.alTerminar(prestamo));
        } catch (PrestamoException e) {
            System.out.println("[" + hilo + "] Préstamo rechazado: " + e.getMessage());
            SwingUtilities.invokeLater(() -> listener.alFallar(e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            SwingUtilities.invokeLater(() -> listener.alFallar("El proceso fue interrumpido."));
        }
    }
}
