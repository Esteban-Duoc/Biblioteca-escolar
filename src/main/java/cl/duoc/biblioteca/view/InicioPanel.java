package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.model.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class InicioPanel extends JPanel {
    public InicioPanel(Usuario usuario, Consumer<String> navegar) {
        super(new BorderLayout());
        setBackground(UI.FONDO);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(UI.encabezado("Inicio", usuario.getMensajeBienvenida()), BorderLayout.NORTH);

        List<String[]> accesos = new ArrayList<>();
        if (usuario.puedeGestionarInventario()) {
            accesos.add(new String[]{"Libros", "Registrar, modificar, eliminar y buscar libros del inventario."});
            accesos.add(new String[]{"Categorías", "Administrar las categorías de los libros."});
            accesos.add(new String[]{"Estudiantes", "Registrar estudiantes y su cuenta de acceso."});
        }
        if (usuario.puedeGestionarPrestamos()) {
            accesos.add(new String[]{"Préstamos", "Prestar y recibir libros, detectar atrasos y probar la concurrencia."});
            accesos.add(new String[]{"Reportes", "Más prestados, historial por estudiante y libros en préstamo."});
        } else {
            accesos.add(new String[]{"Catálogo", "Buscar libros disponibles y solicitar un préstamo."});
            accesos.add(new String[]{"Mis préstamos", "Ver tus libros, devolverlos y revisar tu historial."});
        }

        JPanel grilla = new JPanel(new GridLayout(0, 3, 16, 16));
        grilla.setOpaque(false);
        for (String[] a : accesos) {
            grilla.add(tarjetaAcceso(a[0], a[1], navegar));
        }
        JPanel contenedor = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        contenedor.setOpaque(false);
        grilla.setPreferredSize(new Dimension(840, accesos.size() > 3 ? 280 : 132));
        contenedor.add(grilla);
        add(contenedor, BorderLayout.CENTER);
    }

    private JPanel tarjetaAcceso(String modulo, String descripcion, Consumer<String> navegar) {
        JPanel t = UI.tarjeta(null);
        t.setLayout(new BorderLayout(0, 8));
        JLabel titulo = new JLabel(modulo);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        titulo.setForeground(UI.PRIMARIO);
        JLabel desc = new JLabel("<html>" + descripcion + "</html>");
        desc.setForeground(UI.TEXTO_SUAVE);
        JButton ir = UI.botonPrimario("Abrir");
        ir.addActionListener(e -> navegar.accept(modulo));
        ir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pie.setOpaque(false);
        pie.add(ir);
        t.add(titulo, BorderLayout.NORTH);
        t.add(desc, BorderLayout.CENTER);
        t.add(pie, BorderLayout.SOUTH);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, UI.PRIMARIO_CLARO),
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xD6DBE3)),
                        BorderFactory.createEmptyBorder(12, 14, 12, 14))));
        return t;
    }
}
