package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.CategoriaDAO;
import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.exception.ValidacionException;
import cl.duoc.biblioteca.model.Categoria;
import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.util.ErroresBD;

import java.sql.SQLException;
import java.util.List;

public class LibroController {
    private final LibroDAO libroDAO = new LibroDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public List<Libro> buscar(String texto) throws BibliotecaException {
        try {
            return libroDAO.buscar(texto);
        } catch (SQLException e) {
            throw ErroresBD.traducir("buscar libros", e);
        }
    }

    public List<Libro> listarDisponibles() throws BibliotecaException {
        try {
            return libroDAO.listarDisponibles();
        } catch (SQLException e) {
            throw ErroresBD.traducir("listar libros disponibles", e);
        }
    }

    public List<Categoria> listarCategorias() throws BibliotecaException {
        try {
            return categoriaDAO.listar();
        } catch (SQLException e) {
            throw ErroresBD.traducir("listar categorías", e);
        }
    }

    public void guardar(Libro libro) throws BibliotecaException {
        libro.validar();
        try {
            if (libroDAO.existeIsbn(libro.getIsbn(), libro.getId())) {
                throw new ValidacionException("Ya existe otro libro con el ISBN " + libro.getIsbn() + ".");
            }
            if (libro.getId() == 0) {
                libroDAO.insertar(libro);
            } else {
                libroDAO.actualizar(libro);
            }
        } catch (SQLException e) {
            throw ErroresBD.traducir("guardar el libro", e);
        }
    }

    public void eliminar(Libro libro) throws BibliotecaException {
        try {
            int prestamos = libroDAO.contarPrestamos(libro.getId());
            if (prestamos > 0) {
                throw new BibliotecaException("No se puede eliminar \"" + libro.getTitulo() + "\": tiene "
                        + prestamos + " préstamo(s) en su historial.\nSi ya no está disponible, deja su stock en 0.");
            }
            libroDAO.eliminar(libro.getId());
        } catch (SQLException e) {
            throw ErroresBD.traducir("eliminar el libro", e);
        }
    }
}
