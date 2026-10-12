package cl.duoc.biblioteca.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {
    private static DatabaseConnection instance;

    private final String url;
    private final String usuario;
    private final String password;
    private Connection connection;

    private DatabaseConnection() {
        Properties props = cargarPropiedades();
        this.url = props.getProperty("db.url",
                "jdbc:mysql://localhost:3306/biblioteca?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago");
        this.usuario = props.getProperty("db.user", "root");
        this.password = props.getProperty("db.password", "");
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            try {
                connection = DriverManager.getConnection(url, usuario, password);
            } catch (SQLException e) {
                throw new SQLException("No se pudo conectar a MySQL (" + url + "). "
                        + "Revisa que el servidor esté encendido, que exista la base 'biblioteca' "
                        + "y que el usuario/contraseña de db.properties sean correctos.\nDetalle: "
                        + e.getMessage(), e.getSQLState(), e);
            }
        }
        return connection;
    }

    public synchronized <T> T ejecutarEnTransaccion(OperacionTransaccional<T> operacion) throws Exception {
        Connection con = getConnection();
        boolean autoCommitOriginal = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            T resultado = operacion.ejecutar(con);
            con.commit();
            return resultado;
        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(autoCommitOriginal);
        }
    }

    public synchronized void cerrar() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
            connection = null;
        }
    }

    public String getUrl() {
        return url;
    }

    private Properties cargarPropiedades() {
        Properties props = new Properties();
        Path externo = Paths.get("db.properties");
        try {
            if (Files.exists(externo)) {
                try (InputStream in = new FileInputStream(externo.toFile())) {
                    props.load(in);
                    return props;
                }
            }
            try (InputStream in = DatabaseConnection.class.getResourceAsStream("/db.properties")) {
                if (in != null) {
                    props.load(in);
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudo leer db.properties, se usarán valores por defecto: " + e.getMessage());
        }
        return props;
    }

    @FunctionalInterface
    public interface OperacionTransaccional<T> {
        T ejecutar(Connection con) throws Exception;
    }
}
