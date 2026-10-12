package cl.duoc.biblioteca.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Prestamo {
    public static final int DIAS_PRESTAMO_POR_DEFECTO = 7;

    private int id;
    private Estudiante estudiante;
    private Libro libro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaVencimiento;
    private boolean devuelto;
    private int diasAtrasoRegistrados;

    public Prestamo() {
    }

    public Prestamo(Estudiante estudiante, Libro libro, LocalDate fechaPrestamo, int diasPrestamo) {
        this.estudiante = estudiante;
        this.libro = libro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaVencimiento = calcularVencimiento(fechaPrestamo, diasPrestamo);
        this.devuelto = false;
    }

    public static LocalDate calcularVencimiento(LocalDate desde, int dias) {
        return desde.plusDays(dias);
    }

    public boolean estaAtrasado(LocalDate hoy) {
        return !devuelto && fechaVencimiento != null && hoy.isAfter(fechaVencimiento);
    }

    public long diasAtraso(LocalDate hoy) {
        if (fechaVencimiento == null || !hoy.isAfter(fechaVencimiento)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(fechaVencimiento, hoy);
    }

    public EstadoPrestamo getEstado(LocalDate hoy) {
        if (devuelto) {
            return EstadoPrestamo.DEVUELTO;
        }
        return estaAtrasado(hoy) ? EstadoPrestamo.ATRASADO : EstadoPrestamo.EN_PRESTAMO;
    }

    public String getDescripcionEstado(LocalDate hoy) {
        EstadoPrestamo estado = getEstado(hoy);
        switch (estado) {
            case ATRASADO:
                return "Atrasado (" + diasAtraso(hoy) + " días)";
            case DEVUELTO:
                return diasAtrasoRegistrados > 0
                        ? "Devuelto con " + diasAtrasoRegistrados + " días de atraso"
                        : "Devuelto";
            default:
                return "En préstamo";
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public boolean isDevuelto() {
        return devuelto;
    }

    public void setDevuelto(boolean devuelto) {
        this.devuelto = devuelto;
    }

    public int getDiasAtrasoRegistrados() {
        return diasAtrasoRegistrados;
    }

    public void setDiasAtrasoRegistrados(int diasAtrasoRegistrados) {
        this.diasAtrasoRegistrados = diasAtrasoRegistrados;
    }
}
