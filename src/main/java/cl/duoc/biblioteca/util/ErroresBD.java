package cl.duoc.biblioteca.util;

import cl.duoc.biblioteca.exception.BibliotecaException;

import java.sql.SQLException;

public final class ErroresBD {
    private static final int DUPLICADO = 1062;
    private static final int FK_PADRE_CON_HIJOS = 1451;
    private static final int FK_NO_EXISTE = 1452;

    private ErroresBD() {
    }

    public static BibliotecaException traducir(String accion, SQLException e) {
        String mensaje;
        switch (e.getErrorCode()) {
            case DUPLICADO:
                mensaje = "Ya existe un registro con ese dato único (RUT o ISBN repetido).";
                break;
            case FK_PADRE_CON_HIJOS:
                mensaje = "No se puede eliminar porque tiene otros registros asociados (por ejemplo, préstamos).";
                break;
            case FK_NO_EXISTE:
                mensaje = "El registro relacionado no existe (revisa la categoría, el libro o el estudiante).";
                break;
            default:
                mensaje = e.getMessage();
        }
        System.err.println("[BD] Error al " + accion + ": " + e.getMessage());
        return new BibliotecaException("Error al " + accion + ":\n" + mensaje, e);
    }
}
