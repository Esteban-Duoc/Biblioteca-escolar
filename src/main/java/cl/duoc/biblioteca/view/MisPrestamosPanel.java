package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.PrestamoController;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Prestamo;
import cl.duoc.biblioteca.service.Sesion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MisPrestamosPanel extends JPanel implements Refrescable {
    private final PrestamoController controller = new PrestamoController();
    private final BarraEstado barraEstado;

    private final DefaultTableModel modeloPrestamos = UI.modeloTabla("N°", "Libro", "Autor", "Fecha préstamo", "Vence", "Estado");
    private final JTable tablaPrestamos = UI.tabla(modeloPrestamos);
    private final DefaultTableModel modeloHistorial = UI.modeloTabla("Fecha", "Movimiento", "Libro", "Días atraso", "Detalle");
    private final JTable tablaHistorial = UI.tabla(modeloHistorial);
    private final JLabel lblResumen = new JLabel(" ");

    private List<Prestamo> prestamos = new ArrayList<>();

    public MisPrestamosPanel(BarraEstado barraEstado) {
        super(new BorderLayout(0, 10));
        this.barraEstado = barraEstado;
        setBackground(UI.FONDO);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        construir();
    }

    private void construir() {
        JPanel norte = new JPanel(new BorderLayout(0, 6));
        norte.setOpaque(false);
        norte.add(UI.encabezado("Mis préstamos", "Revisa tus libros, devuélvelos y consulta tu historial."),
                BorderLayout.NORTH);
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        barra.setOpaque(false);
        JButton btnDevolver = UI.boton("Devolver libro seleccionado", UI.EXITO);
        JButton btnActualizar = UI.botonSecundario("Actualizar");
        barra.add(btnDevolver);
        barra.add(btnActualizar);
        lblResumen.setFont(UI.FUENTE_NEGRITA);
        barra.add(lblResumen);
        norte.add(barra, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        UI.anchos(tablaPrestamos, 45, 230, 160, 110, 110, 260);
        UI.anchos(tablaHistorial, 130, 170, 180, 80, 380);
        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setOpaque(false);
        arriba.add(UI.scroll(tablaPrestamos), BorderLayout.CENTER);
        JPanel abajo = new JPanel(new BorderLayout(0, 4));
        abajo.setOpaque(false);
        JLabel tituloHist = new JLabel("Historial de movimientos");
        tituloHist.setFont(UI.FUENTE_NEGRITA);
        tituloHist.setForeground(UI.PRIMARIO);
        abajo.add(tituloHist, BorderLayout.NORTH);
        abajo.add(UI.scroll(tablaHistorial), BorderLayout.CENTER);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, arriba, abajo);
        split.setResizeWeight(0.55);
        split.setBorder(null);
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);

        btnDevolver.addActionListener(e -> devolver());
        btnActualizar.addActionListener(e -> refrescar());
    }

    @Override
    public void refrescar() {
        Estudiante yo = Sesion.getInstance().getEstudiante();
        if (yo == null) {
            return;
        }
        try {
            prestamos = controller.listarDeEstudiante(yo);
            LocalDate hoy = LocalDate.now();
            modeloPrestamos.setRowCount(0);
            int activos = 0;
            int atrasados = 0;
            for (Prestamo p : prestamos) {
                if (!p.isDevuelto()) {
                    activos++;
                }
                if (p.estaAtrasado(hoy)) {
                    atrasados++;
                }
                modeloPrestamos.addRow(new Object[]{p.getId(), p.getLibro().getTitulo(), p.getLibro().getAutor(),
                        p.getFechaPrestamo(), p.getFechaVencimiento(), p.getDescripcionEstado(hoy)});
            }
            lblResumen.setText("   Libros en tu poder: " + activos + (atrasados > 0 ? "  |  Atrasados: " + atrasados : ""));
            lblResumen.setForeground(atrasados > 0 ? UI.PELIGRO : UI.PRIMARIO);

            modeloHistorial.setRowCount(0);
            for (Object[] fila : controller.movimientosDeEstudiante(yo)) {
                modeloHistorial.addRow(fila);
            }
            barraEstado.mensaje(prestamos.size() + " préstamo(s) en tu historial.");
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
    }

    private void devolver() {
        int fila = tablaPrestamos.getSelectedRow();
        if (fila < 0) {
            UI.advertencia(this, "Selecciona el préstamo que quieres devolver.");
            return;
        }
        int id = (int) modeloPrestamos.getValueAt(tablaPrestamos.convertRowIndexToModel(fila), 0);
        Prestamo p = prestamos.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
        if (p == null) {
            return;
        }
        if (p.isDevuelto()) {
            UI.advertencia(this, "Ese libro ya fue devuelto.");
            return;
        }
        if (!UI.confirmar(this, "¿Devolver \"" + p.getLibro().getTitulo() + "\"?")) {
            return;
        }
        try {
            long atraso = controller.registrarDevolucion(p);
            if (atraso > 0) {
                UI.advertencia(this, "Devolución registrada con " + atraso + " día(s) de atraso.\n"
                        + "Quedó constancia en tu historial.");
            } else {
                UI.info(this, "¡Gracias! Devolviste el libro a tiempo.");
            }
            refrescar();
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
    }
}
