package cl.duoc.biblioteca.model;

import cl.duoc.biblioteca.exception.ValidacionException;

public class Libro implements Validable {
    private int id;
    private String titulo;
    private String autor;
    private String isbn;
    private String editorial;
    private int stock;
    private Categoria categoria;

    public Libro() {
    }

    public Libro(int id, String titulo, String autor, String isbn, String editorial, int stock, Categoria categoria) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.editorial = editorial;
        this.stock = stock;
        this.categoria = categoria;
    }

    public boolean hayStock() {
        return stock > 0;
    }

    @Override
    public void validar() throws ValidacionException {
        if (titulo == null || titulo.isBlank()) {
            throw new ValidacionException("El título es obligatorio.");
        }
        if (titulo.length() > 200) {
            throw new ValidacionException("El título no puede superar 200 caracteres.");
        }
        if (autor == null || autor.isBlank()) {
            throw new ValidacionException("El autor es obligatorio.");
        }
        if (isbn == null || !isbn.matches("^(\\d{9}[\\dXx]|\\d{13})$")) {
            throw new ValidacionException("El ISBN debe tener 10 o 13 dígitos, sin guiones.");
        }
        if (editorial == null || editorial.isBlank()) {
            throw new ValidacionException("La editorial es obligatoria.");
        }
        if (stock < 0) {
            throw new ValidacionException("El stock no puede ser negativo.");
        }
        if (categoria == null) {
            throw new ValidacionException("Debes seleccionar una categoría.");
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo == null ? null : titulo.trim();
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor == null ? null : autor.trim();
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn == null ? null : isbn.trim().replace("-", "");
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial == null ? null : editorial.trim();
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        return titulo + " - " + autor + " (stock: " + stock + ")";
    }
}
