package cl.duoc.biblioteca.model;

import cl.duoc.biblioteca.exception.ValidacionException;

public class Estudiante extends Persona {
    private String curso;

    public Estudiante() {
    }

    public Estudiante(int id, String nombre, String rut, String curso, String correo) {
        super(id, nombre, rut, correo);
        this.curso = curso;
    }

    @Override
    public String getTipo() {
        return "Estudiante " + (curso == null ? "" : curso);
    }

    @Override
    public void validar() throws ValidacionException {
        super.validar();
        if (curso == null || curso.isBlank()) {
            throw new ValidacionException("El curso es obligatorio (ej: 3ro Medio A).");
        }
        if (curso.length() > 20) {
            throw new ValidacionException("El curso no puede superar 20 caracteres.");
        }
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso == null ? null : curso.trim();
    }

    @Override
    public String toString() {
        return nombre + " - " + curso + " (" + rut + ")";
    }
}
