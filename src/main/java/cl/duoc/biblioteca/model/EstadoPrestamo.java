package cl.duoc.biblioteca.model;

public enum EstadoPrestamo {
    EN_PRESTAMO("En préstamo"),
    ATRASADO("Atrasado"),
    DEVUELTO("Devuelto");

    private final String texto;

    EstadoPrestamo(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
