package cl.duoc.biblioteca.exception;

public class PrestamoException extends BibliotecaException {
    public PrestamoException(String mensaje) {
        super(mensaje);
    }

    public PrestamoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
