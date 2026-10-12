package cl.duoc.biblioteca.model;

public class Bibliotecario extends Usuario {
    public Bibliotecario() {
    }

    public Bibliotecario(int id, String nombre, String rut, String correo, String contrasena) {
        super(id, nombre, rut, correo, contrasena);
    }

    @Override
    public String getTipo() {
        return "Bibliotecario/a";
    }

    @Override
    public Rol getRol() {
        return Rol.BIBLIOTECARIO;
    }

    @Override
    public boolean puedeGestionarInventario() {
        return true;
    }

    @Override
    public boolean puedeGestionarPrestamos() {
        return true;
    }

    @Override
    public String getMensajeBienvenida() {
        return "Bienvenido/a " + nombre + ". Tienes acceso total a la gestión de la biblioteca.";
    }
}
