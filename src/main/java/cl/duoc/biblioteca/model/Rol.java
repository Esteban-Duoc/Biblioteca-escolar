package cl.duoc.biblioteca.model;

public enum Rol {
    BIBLIOTECARIO("bibliotecario"),
    ESTUDIANTE("estudiante");

    private final String valorBD;

    Rol(String valorBD) {
        this.valorBD = valorBD;
    }

    public String getValorBD() {
        return valorBD;
    }

    public static Rol desdeBD(String valor) {
        for (Rol r : values()) {
            if (r.valorBD.equalsIgnoreCase(valor)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Rol desconocido: " + valor);
    }
}
