package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.config.DatabaseConnection;
import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.UsuarioDAO;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.exception.ValidacionException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Rol;
import cl.duoc.biblioteca.model.Usuario;
import cl.duoc.biblioteca.model.UsuarioEstudiante;
import cl.duoc.biblioteca.util.ErroresBD;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EstudianteController {
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public List<Estudiante> buscar(String texto) throws BibliotecaException {
        try {
            return estudianteDAO.buscar(texto);
        } catch (SQLException e) {
            throw ErroresBD.traducir("buscar estudiantes", e);
        }
    }

    public Set<String> rutsConCuenta() throws BibliotecaException {
        try {
            Set<String> ruts = new HashSet<>();
            for (Usuario u : usuarioDAO.listar()) {
                ruts.add(u.getRut());
            }
            return ruts;
        } catch (SQLException e) {
            throw ErroresBD.traducir("listar usuarios", e);
        }
    }

    public void guardar(Estudiante estudiante, String contrasena, String rutAnterior) throws BibliotecaException {
        estudiante.validar();
        boolean esNuevo = estudiante.getId() == 0;
        boolean traeContrasena = contrasena != null && !contrasena.isEmpty();
        if (esNuevo && !traeContrasena) {
            throw new ValidacionException("Ingresa una contraseña para que el estudiante pueda iniciar sesión.");
        }
        if (traeContrasena && contrasena.length() < 4) {
            throw new ValidacionException("La contraseña debe tener al menos 4 caracteres.");
        }
        try {
            DatabaseConnection.getInstance().ejecutarEnTransaccion(con -> {
                if (estudianteDAO.existeRut(estudiante.getRut(), estudiante.getId())) {
                    throw new ValidacionException("Ya existe un estudiante con el RUT " + estudiante.getRut() + ".");
                }
                Usuario otroConEseRut = usuarioDAO.buscarPorRut(estudiante.getRut());
                if (otroConEseRut != null && otroConEseRut.getRol() == Rol.BIBLIOTECARIO) {
                    throw new ValidacionException("El RUT " + estudiante.getRut() + " pertenece a un bibliotecario.");
                }

                if (esNuevo) {
                    estudianteDAO.insertar(estudiante);
                } else {
                    estudianteDAO.actualizar(estudiante);
                }

                String rutBusqueda = (rutAnterior == null || rutAnterior.isBlank()) ? estudiante.getRut() : rutAnterior;
                Usuario cuenta = usuarioDAO.buscarPorRut(rutBusqueda);
                if (cuenta == null && otroConEseRut != null) {
                    cuenta = otroConEseRut;
                }
                if (cuenta != null) {
                    cuenta.setNombre(estudiante.getNombre());
                    cuenta.setRut(estudiante.getRut());
                    cuenta.setCorreo(estudiante.getCorreo());
                    if (traeContrasena) {
                        cuenta.setContrasena(contrasena);
                    }
                    usuarioDAO.actualizar(cuenta);
                } else if (traeContrasena) {
                    Usuario nueva = new UsuarioEstudiante(0, estudiante.getNombre(), estudiante.getRut(),
                            estudiante.getCorreo(), contrasena);
                    usuarioDAO.insertar(nueva);
                }
                return null;
            });
        } catch (BibliotecaException e) {
            if (esNuevo) {
                estudiante.setId(0);
            }
            throw e;
        } catch (SQLException e) {
            if (esNuevo) {
                estudiante.setId(0);
            }
            throw ErroresBD.traducir("guardar el estudiante", e);
        } catch (Exception e) {
            if (esNuevo) {
                estudiante.setId(0);
            }
            throw new BibliotecaException("Error al guardar el estudiante: " + e.getMessage(), e);
        }
    }

    public void eliminar(Estudiante estudiante) throws BibliotecaException {
        try {
            int prestamos = estudianteDAO.contarPrestamos(estudiante.getId());
            if (prestamos > 0) {
                throw new BibliotecaException("No se puede eliminar a " + estudiante.getNombre() + ": tiene "
                        + prestamos + " préstamo(s) en su historial.");
            }
            DatabaseConnection.getInstance().ejecutarEnTransaccion(con -> {
                estudianteDAO.eliminar(estudiante.getId());
                Usuario cuenta = usuarioDAO.buscarPorRut(estudiante.getRut());
                if (cuenta != null && cuenta.getRol() == Rol.ESTUDIANTE) {
                    usuarioDAO.eliminar(cuenta.getId());
                }
                return null;
            });
        } catch (BibliotecaException e) {
            throw e;
        } catch (SQLException e) {
            throw ErroresBD.traducir("eliminar el estudiante", e);
        } catch (Exception e) {
            throw new BibliotecaException("Error al eliminar el estudiante: " + e.getMessage(), e);
        }
    }
}
