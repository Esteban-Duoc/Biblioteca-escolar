package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.exception.ValidacionException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.reporte.Reporte;
import cl.duoc.biblioteca.reporte.ReporteHistorialEstudiante;
import cl.duoc.biblioteca.reporte.ReporteLibrosEnPrestamo;
import cl.duoc.biblioteca.reporte.ReporteLibrosMasPrestados;
import cl.duoc.biblioteca.util.ErroresBD;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReporteController {
    public static final String MAS_PRESTADOS = "Libros más prestados";
    public static final String HISTORIAL_ESTUDIANTE = "Historial por estudiante";
    public static final String EN_PRESTAMO = "Libros actualmente en préstamo";

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    public String[] tiposDeReporte() {
        return new String[]{MAS_PRESTADOS, HISTORIAL_ESTUDIANTE, EN_PRESTAMO};
    }

    public Reporte crearReporte(String tipo, Estudiante estudiante) throws ValidacionException {
        switch (tipo) {
            case MAS_PRESTADOS:
                return new ReporteLibrosMasPrestados();
            case HISTORIAL_ESTUDIANTE:
                if (estudiante == null) {
                    throw new ValidacionException("Selecciona un estudiante para ver su historial.");
                }
                return new ReporteHistorialEstudiante(estudiante);
            case EN_PRESTAMO:
                return new ReporteLibrosEnPrestamo();
            default:
                throw new ValidacionException("Tipo de reporte desconocido.");
        }
    }

    public List<Object[]> generar(Reporte reporte) throws BibliotecaException {
        try {
            return reporte.generar();
        } catch (SQLException e) {
            throw ErroresBD.traducir("generar el reporte", e);
        }
    }

    public List<Estudiante> listarEstudiantes() throws BibliotecaException {
        try {
            return estudianteDAO.listar();
        } catch (SQLException e) {
            throw ErroresBD.traducir("listar estudiantes", e);
        }
    }

    public void exportarCsv(Reporte reporte, List<Object[]> filas, File archivo) throws BibliotecaException {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        try (PrintWriter out = new PrintWriter(archivo, StandardCharsets.UTF_8)) {
            out.print('﻿');
            out.println(reporte.getTitulo() + " - generado el " + LocalDate.now().format(formato));
            out.println(String.join(";", reporte.getColumnas()));
            for (Object[] fila : filas) {
                StringBuilder linea = new StringBuilder();
                for (int i = 0; i < fila.length; i++) {
                    if (i > 0) {
                        linea.append(';');
                    }
                    Object valor = fila[i];
                    String texto = valor instanceof LocalDate ? ((LocalDate) valor).format(formato)
                            : String.valueOf(valor == null ? "" : valor);
                    linea.append(texto.replace(";", ","));
                }
                out.println(linea);
            }
        } catch (IOException e) {
            throw new BibliotecaException("No se pudo guardar el archivo: " + e.getMessage(), e);
        }
    }
}
