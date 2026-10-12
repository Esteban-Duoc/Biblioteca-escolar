package cl.duoc.biblioteca.model;

public final class UsuarioFactory {
    private UsuarioFactory() {
    }

    public static Usuario crear(Rol rol) {
        switch (rol) {
            case BIBLIOTECARIO:
                return new Bibliotecario();
            case ESTUDIANTE:
                return new UsuarioEstudiante();
            default:
                throw new IllegalArgumentException("Rol no soportado: " + rol);
        }
    }
}
