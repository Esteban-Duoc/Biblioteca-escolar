package cl.duoc.biblioteca.view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class UI {
    public static final Color PRIMARIO = new Color(0x1F3A5F);
    public static final Color PRIMARIO_CLARO = new Color(0x2F6DB5);
    public static final Color FONDO = new Color(0xF3F5F8);
    public static final Color BLANCO = Color.WHITE;
    public static final Color BORDE = new Color(0xD6DBE3);
    public static final Color TEXTO_SUAVE = new Color(0x5F6B7A);
    public static final Color EXITO = new Color(0x2E7D32);
    public static final Color PELIGRO = new Color(0xC62828);
    public static final Color ADVERTENCIA = new Color(0xE65100);

    public static final Font FUENTE = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FUENTE_NEGRITA = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FUENTE_SUBTITULO = new Font("SansSerif", Font.PLAIN, 13);

    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private UI() {
    }

    public static void aplicarLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            UIManager.put("control", FONDO);
            UIManager.put("nimbusBase", PRIMARIO);
            UIManager.put("nimbusFocus", PRIMARIO_CLARO);
            UIManager.put("nimbusSelectionBackground", PRIMARIO_CLARO);
            UIManager.put("defaultFont", FUENTE);
        } catch (Exception e) {
            System.err.println("No se pudo aplicar Nimbus: " + e.getMessage());
        }
    }

    public static JButton boton(String texto, Color color) {
        JButton b = new JButton(texto);
        b.setFont(FUENTE_NEGRITA);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton botonPrimario(String texto) {
        return boton(texto, PRIMARIO_CLARO);
    }

    public static JButton botonPeligro(String texto) {
        return boton(texto, PELIGRO);
    }

    public static JButton botonSecundario(String texto) {
        JButton b = new JButton(texto);
        b.setFont(FUENTE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JPanel encabezado(String titulo, String subtitulo) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel t = new JLabel(titulo);
        t.setFont(FUENTE_TITULO);
        t.setForeground(PRIMARIO);
        JLabel s = new JLabel(subtitulo);
        s.setFont(FUENTE_SUBTITULO);
        s.setForeground(TEXTO_SUAVE);
        p.add(t, BorderLayout.NORTH);
        p.add(s, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        return p;
    }

    public static JPanel tarjeta(String titulo) {
        JPanel p = new JPanel();
        p.setBackground(BLANCO);
        Border linea = BorderFactory.createLineBorder(BORDE);
        Border interior = BorderFactory.createEmptyBorder(10, 12, 12, 12);
        if (titulo != null) {
            p.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(linea, titulo, 0, 0, FUENTE_NEGRITA, PRIMARIO), interior));
        } else {
            p.setBorder(BorderFactory.createCompoundBorder(linea, interior));
        }
        return p;
    }

    public static DefaultTableModel modeloTabla(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columna) {
                for (int fila = 0; fila < getRowCount(); fila++) {
                    Object valor = getValueAt(fila, columna);
                    if (valor != null) {
                        return valor.getClass();
                    }
                }
                return Object.class;
            }
        };
    }

    public static JTable tabla(DefaultTableModel modelo) {
        JTable t = new JTable(modelo);
        t.setRowHeight(26);
        t.setFont(FUENTE);
        t.getTableHeader().setFont(FUENTE_NEGRITA);
        t.getTableHeader().setReorderingAllowed(false);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(true);
        t.setFillsViewportHeight(true);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setDefaultRenderer(Object.class, new RendererBiblioteca());
        t.setDefaultRenderer(Integer.class, new RendererBiblioteca());
        return t;
    }

    public static void anchos(JTable tabla, int... anchos) {
        for (int i = 0; i < anchos.length && i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    public static JScrollPane scroll(JComponent c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createLineBorder(BORDE));
        s.getViewport().setBackground(BLANCO);
        return s;
    }

    public static void filaFormulario(JPanel panel, int fila, String etiqueta, JComponent campo) {
        if (!(panel.getLayout() instanceof GridBagLayout)) {
            panel.setLayout(new GridBagLayout());
        }
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(4, 0, 4, 8);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        JLabel l = new JLabel(etiqueta);
        l.setFont(FUENTE_NEGRITA);
        panel.add(l, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);
        panel.add(campo, c);
    }

    public static void filaCompleta(JPanel panel, int fila, JComponent comp) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 0, 0, 0);
        panel.add(comp, c);
    }

    public static void relleno(JPanel panel, int fila) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.gridx = 0;
        c.weighty = 1;
        panel.add(new JLabel(), c);
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void advertencia(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    static class RendererBiblioteca extends DefaultTableCellRenderer {
        private static final Color FILA_ALTERNA = new Color(0xF7F9FC);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Object mostrar = value instanceof LocalDate ? ((LocalDate) value).format(FORMATO_FECHA) : value;
            Component c = super.getTableCellRendererComponent(table, mostrar, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setHorizontalAlignment(value instanceof Number ? CENTER : LEFT);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? BLANCO : FILA_ALTERNA);
                c.setForeground(Color.DARK_GRAY);
                setFont(FUENTE);
                if (value instanceof String) {
                    String s = (String) value;
                    if (s.startsWith("Atrasado") || s.startsWith("Devuelto con")) {
                        c.setForeground(PELIGRO);
                        setFont(FUENTE_NEGRITA);
                    } else if (s.equals("En préstamo")) {
                        c.setForeground(EXITO);
                        setFont(FUENTE_NEGRITA);
                    } else if (s.equals("Devuelto")) {
                        c.setForeground(TEXTO_SUAVE);
                    } else if (s.startsWith("Sin stock")) {
                        c.setForeground(PELIGRO);
                    }
                }
            }
            return c;
        }
    }
}
