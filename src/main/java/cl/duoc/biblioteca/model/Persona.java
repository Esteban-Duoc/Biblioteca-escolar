package cl.duoc.biblioteca.model;

import cl.duoc.biblioteca.exception.ValidacionException;

public abstract class Persona implements Validable {
    private static final String PATRON_RUT = "^\\d{7,8}-[\\dkK]$";
    private static final String PATRON_CORREO = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";

    protected int id;
    protected String nombre;
    protected String rut;
    protected String correo;

    protected Persona() {
    }

    protected Persona(int id, String nombre, String rut, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.rut = rut;
        this.correo = correo;
    }

    public abstract String getTipo();

    @Override
    public void validar() throws ValidacionException {
        if (nombre == null || nombre.isBlank()) {
            throw new ValidacionException("El nombre es obligatorio.");
        }
        if (nombre.length() > 100) {
            throw new ValidacionException("El nombre no puede superar 100 caracteres.");
        }
        if (rut == null || !rut.matches(PATRON_RUT)) {
            throw new ValidacionException("El RUT debe tener el formato 12345678-9 (sin puntos, con guion).");
        }
        if (correo == null || !correo.matches(PATRON_CORREO)) {
            throw new ValidacionException("El correo no tiene un formato válido (ej: nombre@correo.cl).");
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

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut == null ? null : rut.trim();
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo == null ? null : correo.trim();
    }

    @Override
    public String toString() {
        return nombre + " (" + rut + ")";
    }
}
