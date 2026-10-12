package cl.duoc.biblioteca.model;

import cl.duoc.biblioteca.exception.ValidacionException;

public interface Validable {
    void validar() throws ValidacionException;
}
