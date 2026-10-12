package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.LoginController;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

public class LoginFrame extends JFrame {
    private final LoginController controller = new LoginController();
    private final JTextField txtUsuario = new JTextField(20);
    private final JPasswordField txtContrasena = new JPasswordField(20);
    private final JButton btnIngresar = UI.botonPrimario("Ingresar");
    private final JLabel lblEstado = new JLabel(" ");

    public LoginFrame() {
        super("Biblioteca Escolar - Iniciar sesión");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(820, 500);
        setMinimumSize(new Dimension(700, 460));
        setLocationRelativeTo(null);
        construir();
    }

    private void construir() {
        JPanel raiz = new JPanel(new GridLayout(1, 2));

        JPanel izquierda = new JPanel(new GridBagLayout());
        izquierda.setBackground(UI.PRIMARIO);
        JPanel textos = new JPanel(new GridLayout(0, 1, 0, 6));
        textos.setOpaque(false);
        JLabel titulo = new JLabel("Biblioteca Escolar");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 30));
        titulo.setForeground(Color.WHITE);
        JLabel sub1 = new JLabel("Sistema de gestión de préstamos");
        JLabel sub2 = new JLabel("libros · estudiantes · reportes");
        for (JLabel l : new JLabel[]{sub1, sub2}) {
            l.setFont(new Font("SansSerif", Font.PLAIN, 15));
            l.setForeground(new Color(0xC9D6EA));
        }
        textos.add(titulo);
        textos.add(sub1);
        textos.add(sub2);
        izquierda.add(textos);

        JPanel derecha = new JPanel(new GridBagLayout());
        derecha.setBackground(UI.BLANCO);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(300, 330));

        JLabel bienvenida = new JLabel("Iniciar sesión");
        bienvenida.setFont(UI.FUENTE_TITULO);
        bienvenida.setForeground(UI.PRIMARIO);
        JLabel ayuda = new JLabel("<html>Ingresa con tu <b>correo</b> o <b>RUT</b> y tu contraseña.</html>");
        ayuda.setForeground(UI.TEXTO_SUAVE);

        lblEstado.setForeground(UI.PELIGRO);
        btnIngresar.setPreferredSize(new Dimension(120, 36));

        JLabel prueba = new JLabel("<html><span style='color:#5F6B7A'>Usuarios de prueba (clave123):<br>"
                + "Bibliotecario: antonia@correo.cl<br>Estudiante: carlos@correo.cl</span></html>");
        prueba.setFont(new Font("SansSerif", Font.PLAIN, 11));

        UI.filaCompleta(form, 0, bienvenida);
        UI.filaCompleta(form, 1, ayuda);
        UI.filaCompleta(form, 2, new JLabel("Correo o RUT"));
        UI.filaCompleta(form, 3, txtUsuario);
        UI.filaCompleta(form, 4, new JLabel("Contraseña"));
        UI.filaCompleta(form, 5, txtContrasena);
        UI.filaCompleta(form, 6, btnIngresar);
        UI.filaCompleta(form, 7, lblEstado);
        UI.filaCompleta(form, 8, prueba);
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        derecha.add(form);

        raiz.add(izquierda);
        raiz.add(derecha);
        setContentPane(raiz);

        btnIngresar.addActionListener(e -> ingresar());
        txtContrasena.addActionListener(e -> ingresar());
        txtUsuario.addActionListener(e -> txtContrasena.requestFocusInWindow());
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void ingresar() {
        String usuario = txtUsuario.getText();
        String contrasena = new String(txtContrasena.getPassword());
        btnIngresar.setEnabled(false);
        lblEstado.setForeground(UI.TEXTO_SUAVE);
        lblEstado.setText("Verificando...");

        new Thread(() -> {
            try {
                Usuario u = controller.iniciarSesion(usuario, contrasena);
                SwingUtilities.invokeLater(() -> abrirPrincipal(u));
            } catch (BibliotecaException ex) {
                SwingUtilities.invokeLater(() -> mostrarError(ex.getMessage()));
            }
        }, "Hilo-Login").start();
    }

    private void abrirPrincipal(Usuario usuario) {
        new MainFrame(usuario).setVisible(true);
        dispose();
    }

    private void mostrarError(String mensaje) {
        btnIngresar.setEnabled(true);
        lblEstado.setForeground(UI.PELIGRO);
        lblEstado.setText("No se pudo iniciar sesión.");
        txtContrasena.setText("");
        UI.error(this, mensaje);
        txtContrasena.requestFocusInWindow();
    }
}
