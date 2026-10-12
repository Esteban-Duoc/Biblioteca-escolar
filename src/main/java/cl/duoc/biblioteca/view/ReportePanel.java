package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.ReporteController;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.reporte.Reporte;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReportePanel extends JPanel implements Refrescable {
    private final ReporteController controller = new ReporteController();
    private final BarraEstado barraEstado;

    private final JComboBox<String> cmbTipo;
    private final JComboBox<Estudiante> cmbEstudiante = new JComboBox<>();
    private final JLabel lblEstudiante = new JLabel("Estudiante:");
    private final JLabel lblTitulo = new JLabel(" ");
    private final JLabel lblDescripcion = new JLabel(" ");
    private final DefaultTableModel modelo = UI.modeloTabla();
    private final JTable tabla = UI.tabla(modelo);

    private Reporte reporteActual;
    private List<Object[]> filasActuales = new ArrayList<>();

    public ReportePanel(BarraEstado barraEstado) {
        super(new BorderLayout(0, 10));
        this.barraEstado = barraEstado;
        this.cmbTipo = new JComboBox<>(controller.tiposDeReporte());
        setBackground(UI.FONDO);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        construir();
    }

    private void construir() {
        JPanel norte = new JPanel(new BorderLayout(0, 6));
        norte.setOpaque(false);
        norte.add(UI.encabezado("Reportes", "Informes para apoyar la toma de decisiones de la biblioteca."),
                BorderLayout.NORTH);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.setOpaque(false);
        JButton btnGenerar = UI.botonPrimario("Generar");
        JButton btnExportar = UI.botonSecundario("Exportar a CSV (Excel)");
        cmbTipo.setPreferredSize(new Dimension(260, 28));
        cmbEstudiante.setPreferredSize(new Dimension(280, 28));
        filtros.add(new JLabel("Reporte:"));
        filtros.add(cmbTipo);
        filtros.add(lblEstudiante);
        filtros.add(cmbEstudiante);
        filtros.add(btnGenerar);
        filtros.add(btnExportar);
        norte.add(filtros, BorderLayout.CENTER);

        JPanel info = new JPanel(new BorderLayout());
        info.setOpaque(false);
        info.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        lblTitulo.setFont(UI.FUENTE_NEGRITA.deriveFont(15f));
        lblTitulo.setForeground(UI.PRIMARIO);
        lblDescripcion.setForeground(UI.TEXTO_SUAVE);
        info.add(lblTitulo, BorderLayout.NORTH);
        info.add(lblDescripcion, BorderLayout.CENTER);
        norte.add(info, BorderLayout.SOUTH);

        add(norte, BorderLayout.NORTH);
        add(UI.scroll(tabla), BorderLayout.CENTER);

        cmbTipo.addActionListener(e -> {
            actualizarFiltros();
            generar();
        });
        cmbEstudiante.addActionListener(e -> {
            if (ReporteController.HISTORIAL_ESTUDIANTE.equals(cmbTipo.getSelectedItem())) {
                generar();
            }
        });
        btnGenerar.addActionListener(e -> generar());
        btnExportar.addActionListener(e -> exportar());
        actualizarFiltros();
    }

    @Override
    public void refrescar() {
        try {
            Estudiante sel = (Estudiante) cmbEstudiante.getSelectedItem();
            DefaultComboBoxModel<Estudiante> m = new DefaultComboBoxModel<>();
            for (Estudiante e : controller.listarEstudiantes()) {
                m.addElement(e);
            }
            cmbEstudiante.setModel(m);
            if (sel != null) {
                for (int i = 0; i < m.getSize(); i++) {
                    if (m.getElementAt(i).getId() == sel.getId()) {
                        cmbEstudiante.setSelectedIndex(i);
                    }
                }
            }
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
        generar();
    }

    private void actualizarFiltros() {
        boolean historial = ReporteController.HISTORIAL_ESTUDIANTE.equals(cmbTipo.getSelectedItem());
        lblEstudiante.setVisible(historial);
        cmbEstudiante.setVisible(historial);
    }

    private void generar() {
        try {
            reporteActual = controller.crearReporte((String) cmbTipo.getSelectedItem(),
                    (Estudiante) cmbEstudiante.getSelectedItem());
            filasActuales = controller.generar(reporteActual);
            modelo.setColumnIdentifiers(reporteActual.getColumnas());
            modelo.setRowCount(0);
            for (Object[] fila : filasActuales) {
                modelo.addRow(fila);
            }
            int ultima = tabla.getColumnCount() - 1;
            tabla.getColumnModel().getColumn(ultima).setPreferredWidth(260);
            lblTitulo.setText(reporteActual.getTitulo() + "  (" + filasActuales.size() + " registros)");
            lblDescripcion.setText(reporteActual.getDescripcion());
            barraEstado.mensaje("Reporte generado: " + reporteActual.getTitulo());
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
    }

    private void exportar() {
        if (reporteActual == null || filasActuales.isEmpty()) {
            UI.advertencia(this, "Genera un reporte con datos antes de exportar.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(reporteActual.getTitulo().replace(' ', '_') + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = chooser.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".csv")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".csv");
        }
        try {
            controller.exportarCsv(reporteActual, filasActuales, archivo);
            UI.info(this, "Reporte exportado en:\n" + archivo.getAbsolutePath());
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
    }
}
