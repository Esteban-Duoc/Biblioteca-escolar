package cl.duoc.biblioteca.model;

public class UsuarioEstudiante extends Usuario {
    public UsuarioEstudiante() {
    }

    public UsuarioEstudiante(int id, String nombre, String rut, String correo, String contrasena) {
        super(id, nombre, rut, correo, contrasena);
    }

    @Override
    public String getTipo() {
        return "Estudiante";
    }

    @Override
    public Rol getRol() {
        return Rol.ESTUDIANTE;
    }

    @Override
    public boolean puedeGestionarInventario() {
        return false;
    }

    @Override
    public boolean puedeGestionarPrestamos() {
        return false;
    }

    @Override
    public String getMensajeBienvenida() {
        return "Hola " + nombre + ". Puedes consultar el catálogo, pedir y devolver tus libros.";
    }
}
