package cl.duoc.biblioteca.service;

public interface ResultadoListener<T> {
    void alTerminar(T resultado);

    void alFallar(String mensaje);
}
