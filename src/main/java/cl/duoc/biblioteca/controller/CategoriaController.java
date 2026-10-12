package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.CategoriaDAO;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Categoria;
import cl.duoc.biblioteca.util.ErroresBD;

import java.sql.SQLException;
import java.util.List;

public class CategoriaController {
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public List<Categoria> listar() throws BibliotecaException {
        try {
            return categoriaDAO.listar();
        } catch (SQLException e) {
            throw ErroresBD.traducir("listar categorías", e);
        }
    }

    public void guardar(Categoria categoria) throws BibliotecaException {
        categoria.validar();
        try {
            if (categoria.getId() == 0) {
                categoriaDAO.insertar(categoria);
            } else {
                categoriaDAO.actualizar(categoria);
            }
        } catch (SQLException e) {
            throw ErroresBD.traducir("guardar la categoría", e);
        }
    }

    public void eliminar(Categoria categoria) throws BibliotecaException {
        try {
            int libros = categoriaDAO.contarLibros(categoria.getId());
            if (libros > 0) {
                throw new BibliotecaException("No se puede eliminar \"" + categoria.getNombre() + "\": tiene "
                        + libros + " libro(s) asociados. Cámbialos de categoría primero.");
            }
            categoriaDAO.eliminar(categoria.getId());
        } catch (SQLException e) {
            throw ErroresBD.traducir("eliminar la categoría", e);
        }
    }
}
