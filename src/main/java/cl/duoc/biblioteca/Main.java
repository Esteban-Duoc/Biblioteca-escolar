package cl.duoc.biblioteca;

import cl.duoc.biblioteca.config.DatabaseConnection;
import cl.duoc.biblioteca.dao.HistorialDAO;
import cl.duoc.biblioteca.view.LoginFrame;
import cl.duoc.biblioteca.view.UI;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UI.aplicarLookAndFeel();
            verificarBaseDeDatos();
            new LoginFrame().setVisible(true);
        });
    }

    private static void verificarBaseDeDatos() {
        try {
            DatabaseConnection.getInstance().getConnection();
            new HistorialDAO().crearTablaSiNoExiste();
            System.out.println("Conexión a MySQL OK: " + DatabaseConnection.getInstance().getUrl());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage() + "\n\nPuedes corregir db.properties y volver a abrir la aplicación.",
                    "Sin conexión a la base de datos", JOptionPane.ERROR_MESSAGE);
        }
    }
}
