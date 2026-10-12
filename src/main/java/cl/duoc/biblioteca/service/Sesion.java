package cl.duoc.biblioteca.service;

import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Usuario;

public final class Sesion {
    private static final Sesion INSTANCIA = new Sesion();

    private Usuario usuario;
    private Estudiante estudiante;

    private Sesion() {
    }

    public static Sesion getInstance() {
        return INSTANCIA;
    }

    public void iniciar(Usuario usuario, Estudiante estudiante) {
        this.usuario = usuario;
        this.estudiante = estudiante;
    }

    public void cerrar() {
        usuario = null;
        estudiante = null;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }
}
