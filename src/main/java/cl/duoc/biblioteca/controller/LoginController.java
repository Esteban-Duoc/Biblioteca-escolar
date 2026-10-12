package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.UsuarioDAO;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.exception.ValidacionException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Rol;
import cl.duoc.biblioteca.model.Usuario;
import cl.duoc.biblioteca.service.Sesion;
import cl.duoc.biblioteca.util.ErroresBD;

import java.sql.SQLException;

public class LoginController {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    public Usuario iniciarSesion(String correoORut, String contrasena) throws BibliotecaException {
        if (correoORut == null || correoORut.isBlank() || contrasena == null || contrasena.isEmpty()) {
            throw new ValidacionException("Ingresa tu correo (o RUT) y tu contraseña.");
        }
        try {
            Usuario usuario = usuarioDAO.autenticar(correoORut.trim(), contrasena);
            if (usuario == null) {
                throw new ValidacionException("Usuario o contraseña incorrectos.");
            }
            Estudiante estudiante = null;
            if (usuario.getRol() == Rol.ESTUDIANTE) {
                estudiante = estudianteDAO.buscarPorRut(usuario.getRut());
                if (estudiante == null) {
                    throw new ValidacionException("Tu cuenta no está vinculada a un estudiante registrado.\n"
                            + "Pide al bibliotecario que te registre en Gestión de estudiantes.");
                }
            }
            Sesion.getInstance().iniciar(usuario, estudiante);
            return usuario;
        } catch (SQLException e) {
            throw ErroresBD.traducir("iniciar sesión", e);
        }
    }

    public void cerrarSesion() {
        Sesion.getInstance().cerrar();
    }
}
