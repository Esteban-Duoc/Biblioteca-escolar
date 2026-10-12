package cl.duoc.biblioteca.service;

import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.exception.PrestamoException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.model.Prestamo;

import javax.swing.SwingUtilities;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class PruebaConcurrencia {
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public interface Observador {
        void registrar(String linea);

        void terminado(Resultado resultado);
    }

    public static class Resultado {
        public final int stockInicial;
        public final int aceptados;
        public final int rechazados;
        public final int stockFinal;
        public final boolean consistente;

        Resultado(int stockInicial, int aceptados, int rechazados, int stockFinal) {
            this.stockInicial = stockInicial;
            this.aceptados = aceptados;
            this.rechazados = rechazados;
            this.stockFinal = stockFinal;
            this.consistente = stockFinal >= 0
                    && aceptados <= stockInicial
                    && stockFinal == stockInicial - aceptados;
        }
    }

    private final LibroDAO libroDAO = new LibroDAO();

    public void ejecutar(Libro libro, List<Estudiante> estudiantes, boolean revertirAlFinal, Observador obs) {
        Thread coordinador = new Thread(() -> correr(libro, estudiantes, revertirAlFinal, obs), "Hilo-Coordinador");
        coordinador.setDaemon(true);
        coordinador.start();
    }

    private void correr(Libro libro, List<Estudiante> estudiantes, boolean revertir, Observador obs) {
        try {
            int stockInicial = libroDAO.buscarPorId(libro.getId()).getStock();
            log(obs, "Libro: \"" + libro.getTitulo() + "\" | stock inicial = " + stockInicial);
            log(obs, "Lanzando " + estudiantes.size() + " hilos al mismo tiempo...");

            CountDownLatch partida = new CountDownLatch(1);
            CountDownLatch fin = new CountDownLatch(estudiantes.size());
            AtomicInteger aceptados = new AtomicInteger();
            AtomicInteger rechazados = new AtomicInteger();
            List<Integer> idsCreados = Collections.synchronizedList(new ArrayList<>());

            for (int i = 0; i < estudiantes.size(); i++) {
                Estudiante est = estudiantes.get(i);
                Thread hilo = new Thread(() -> {
                    String nombre = Thread.currentThread().getName();
                    try {
                        partida.await();
                        Prestamo p = PrestamoService.getInstance()
                                .registrarPrestamo(est.getId(), libro.getId(), Prestamo.DIAS_PRESTAMO_POR_DEFECTO);
                        idsCreados.add(p.getId());
                        aceptados.incrementAndGet();
                        log(obs, nombre + " -> ACEPTADO para " + est.getNombre()
                                + " (stock quedó en " + p.getLibro().getStock() + ")");
                    } catch (PrestamoException e) {
                        rechazados.incrementAndGet();
                        log(obs, nombre + " -> RECHAZADO para " + est.getNombre() + ": " + e.getMessage());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        fin.countDown();
                    }
                }, "Hilo-" + (i + 1));
                hilo.start();
            }

            partida.countDown();
            fin.await();

            int stockFinal = libroDAO.buscarPorId(libro.getId()).getStock();
            Resultado r = new Resultado(stockInicial, aceptados.get(), rechazados.get(), stockFinal);
            log(obs, "----------------------------------------------");
            log(obs, "Aceptados: " + r.aceptados + " | Rechazados: " + r.rechazados
                    + " | Stock final: " + r.stockFinal);
            log(obs, r.consistente
                    ? "RESULTADO: stock CONSISTENTE (inicial - aceptados = final, nunca negativo)."
                    : "RESULTADO: ¡INCONSISTENCIA detectada en el stock!");

            if (revertir && !idsCreados.isEmpty()) {
                for (Integer id : idsCreados) {
                    PrestamoService.getInstance().revertirPrestamo(id);
                }
                int stockRestaurado = libroDAO.buscarPorId(libro.getId()).getStock();
                log(obs, "Préstamos de prueba revertidos. Stock restaurado a " + stockRestaurado + ".");
            }
            SwingUtilities.invokeLater(() -> obs.terminado(r));
        } catch (Exception e) {
            log(obs, "Error en la prueba: " + e.getMessage());
            SwingUtilities.invokeLater(() -> obs.terminado(null));
        }
    }

    private void log(Observador obs, String mensaje) {
        String linea = "[" + LocalTime.now().format(HORA) + "] " + mensaje;
        System.out.println(linea);
        SwingUtilities.invokeLater(() -> obs.registrar(linea));
    }
}
