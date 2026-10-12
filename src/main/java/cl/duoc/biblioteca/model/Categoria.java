package cl.duoc.biblioteca.model;

import cl.duoc.biblioteca.exception.ValidacionException;

public class Categoria implements Validable {
    private int id;
    private String nombre;

    public Categoria() {
    }

    public Categoria(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    @Override
    public void validar() throws ValidacionException {
        if (nombre == null || nombre.isBlank()) {
            throw new ValidacionException("El nombre de la categoría es obligatorio.");
        }
        if (nombre.length() > 100) {
            throw new ValidacionException("El nombre de la categoría no puede superar 100 caracteres.");
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre == null ? null : nombre.trim();
    }

    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Categoria && ((Categoria) o).id == id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
