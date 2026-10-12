package cl.duoc.biblioteca.model;

import cl.duoc.biblioteca.exception.ValidacionException;

public abstract class Usuario extends Persona {
    protected String contrasena;

    protected Usuario() {
    }

    protected Usuario(int id, String nombre, String rut, String correo, String contrasena) {
        super(id, nombre, rut, correo);
        this.contrasena = contrasena;
    }

    public abstract Rol getRol();

    public abstract boolean puedeGestionarInventario();

    public abstract boolean puedeGestionarPrestamos();

    public abstract String getMensajeBienvenida();

    @Override
    public void validar() throws ValidacionException {
        super.validar();
        if (contrasena == null || contrasena.length() < 4) {
            throw new ValidacionException("La contraseña debe tener al menos 4 caracteres.");
        }
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
