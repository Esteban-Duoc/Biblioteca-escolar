package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.config.DatabaseConnection;
import cl.duoc.biblioteca.controller.LoginController;
import cl.duoc.biblioteca.model.Usuario;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private static final Color NAV_FONDO = new Color(0x18304F);
    private static final Color NAV_SELECCION = new Color(0x2F6DB5);

    private final Usuario usuario;
    private final CardLayout cards = new CardLayout();
    private final JPanel contenido = new JPanel(cards);
    private final BarraEstado barraEstado = new BarraEstado();
    private final Map<String, Component> pantallas = new LinkedHashMap<>();
    private final Map<String, JButton> botonesNav = new LinkedHashMap<>();

    public MainFrame(Usuario usuario) {
        super("Biblioteca Escolar - " + usuario.getNombre() + " (" + usuario.getTipo() + ")");
        this.usuario = usuario;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setSize(1200, 740);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);

        registrarPantallas();
        construir();
        mostrar("Inicio");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });
    }

    private void registrarPantallas() {
        pantallas.put("Inicio", new InicioPanel(usuario, this::mostrar));
        if (usuario.puedeGestionarInventario()) {
            pantallas.put("Libros", new LibroPanel(barraEstado));
            pantallas.put("Categorías", new CategoriaPanel(barraEstado));
            pantallas.put("Estudiantes", new EstudiantePanel(barraEstado));
        }
        if (usuario.puedeGestionarPrestamos()) {
            pantallas.put("Préstamos", new PrestamoPanel(barraEstado));
            pantallas.put("Reportes", new ReportePanel(barraEstado));
        } else {
            pantallas.put("Catálogo", new CatalogoPanel(barraEstado));
            pantallas.put("Mis préstamos", new MisPrestamosPanel(barraEstado));
        }
        for (Map.Entry<String, Component> p : pantallas.entrySet()) {
            contenido.add(p.getValue(), p.getKey());
        }
    }

    private void construir() {
        setJMenuBar(crearMenu());
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(crearEncabezado(), BorderLayout.NORTH);
        raiz.add(crearNavegacion(), BorderLayout.WEST);
        contenido.setBackground(UI.FONDO);
        raiz.add(contenido, BorderLayout.CENTER);
        raiz.add(barraEstado, BorderLayout.SOUTH);
        setContentPane(raiz);
    }

    private JMenuBar crearMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu archivo = new JMenu("Archivo");
        JMenuItem cerrarSesion = new JMenuItem("Cerrar sesión");
        cerrarSesion.addActionListener(e -> cerrarSesion());
        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(e -> salir());
        archivo.add(cerrarSesion);
        archivo.addSeparator();
        archivo.add(salir);

        JMenu modulos = new JMenu("Módulos");
        for (String nombre : pantallas.keySet()) {
            JMenuItem item = new JMenuItem(nombre);
            item.addActionListener(e -> mostrar(nombre));
            modulos.add(item);
        }

        JMenu ayuda = new JMenu("Ayuda");
        JMenuItem acerca = new JMenuItem("Acerca de");
        acerca.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Sistema de Gestión de Biblioteca Escolar\n"
                        + "Desarrollo Orientado a Objetos II - EFT\n\n"
                        + "Java Swing (JFrame) · MySQL (JDBC)\n"
                        + "Patrones: MVC, DAO y Singleton\n"
                        + "Concurrencia: hilos y métodos synchronized\n\n"
                        + "Base de datos: " + DatabaseConnection.getInstance().getUrl(),
                "Acerca de", JOptionPane.INFORMATION_MESSAGE));
        ayuda.add(acerca);

        barra.add(archivo);
        barra.add(modulos);
        barra.add(ayuda);
        return barra;
    }

    private JPanel crearEncabezado() {
        JPanel enc = new JPanel(new BorderLayout());
        enc.setBackground(UI.PRIMARIO);
        enc.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        JLabel titulo = new JLabel("Biblioteca Escolar");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        derecha.setOpaque(false);
        JLabel quien = new JLabel(usuario.getNombre() + "  |  " + usuario.getTipo());
        quien.setForeground(new Color(0xDCE6F5));
        quien.setFont(UI.FUENTE_NEGRITA);
        JButton btnSalir = UI.boton("Cerrar sesión", new Color(0x3C5A82));
        btnSalir.addActionListener(e -> cerrarSesion());
        derecha.add(quien);
        derecha.add(btnSalir);

        enc.add(titulo, BorderLayout.WEST);
        enc.add(derecha, BorderLayout.EAST);
        return enc;
    }

    private JPanel crearNavegacion() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(NAV_FONDO);
        nav.setPreferredSize(new Dimension(190, 0));
        nav.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        JLabel seccion = new JLabel("  MENÚ");
        seccion.setForeground(new Color(0x8FA6C4));
        seccion.setFont(new Font("SansSerif", Font.BOLD, 11));
        seccion.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 0));
        nav.add(seccion);

        for (String nombre : pantallas.keySet()) {
            JButton b = new JButton(nombre);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            b.setHorizontalAlignment(JButton.LEFT);
            b.setFont(UI.FUENTE_NEGRITA);
            b.setForeground(Color.WHITE);
            b.setBackground(NAV_FONDO);
            b.setContentAreaFilled(false);
            b.setOpaque(true);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 10));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> mostrar(nombre));
            botonesNav.put(nombre, b);
            nav.add(b);
        }
        nav.add(Box.createVerticalGlue());
        return nav;
    }

    public void mostrar(String nombre) {
        Component pantalla = pantallas.get(nombre);
        if (pantalla == null) {
            return;
        }
        cards.show(contenido, nombre);
        for (Map.Entry<String, JButton> b : botonesNav.entrySet()) {
            b.getValue().setBackground(b.getKey().equals(nombre) ? NAV_SELECCION : NAV_FONDO);
        }
        if (pantalla instanceof Refrescable) {
            ((Refrescable) pantalla).refrescar();
        }
        barraEstado.mensaje("Módulo: " + nombre);
    }

    private void cerrarSesion() {
        if (UI.confirmar(this, "¿Cerrar la sesión de " + usuario.getNombre() + "?")) {
            new LoginController().cerrarSesion();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void salir() {
        if (UI.confirmar(this, "¿Deseas salir del sistema?")) {
            DatabaseConnection.getInstance().cerrar();
            dispose();
            System.exit(0);
        }
    }
}
