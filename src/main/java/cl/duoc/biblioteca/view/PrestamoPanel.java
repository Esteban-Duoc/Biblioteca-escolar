package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.PrestamoController;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.model.Prestamo;
import cl.duoc.biblioteca.service.PrestamoService;
import cl.duoc.biblioteca.service.ResultadoListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrestamoPanel extends JPanel implements Refrescable {
    private static final String FILTRO_ACTIVOS = "Solo activos (sin devolver)";
    private static final String FILTRO_TODOS = "Todos los préstamos";

    private final PrestamoController controller = new PrestamoController();
    private final BarraEstado barraEstado;

    private final JComboBox<Estudiante> cmbEstudiante = new JComboBox<>();
    private final JComboBox<Libro> cmbLibro = new JComboBox<>();
    private final JSpinner spnDias = new JSpinner(new SpinnerNumberModel(
            Prestamo.DIAS_PRESTAMO_POR_DEFECTO, PrestamoService.MIN_DIAS, PrestamoService.MAX_DIAS, 1));
    private final JLabel lblVence = new JLabel();
    private final JLabel lblResultado = new JLabel(" ");
    private final JButton btnRegistrar = UI.botonPrimario("Registrar préstamo");

    private final JComboBox<String> cmbFiltro = new JComboBox<>(new String[]{FILTRO_ACTIVOS, FILTRO_TODOS});
    private final DefaultTableModel modelo = UI.modeloTabla("N°", "Estudiante", "Curso", "Libro",
            "Fecha préstamo", "Vence", "Estado");
    private final JTable tabla = UI.tabla(modelo);

    private List<Prestamo> prestamos = new ArrayList<>();
    private int enProceso = 0;

    public PrestamoPanel(BarraEstado barraEstado) {
        super(new BorderLayout(0, 12));
        this.barraEstado = barraEstado;
        setBackground(UI.FONDO);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        construir();
    }

    private void construir() {
        JPanel norte = new JPanel(new BorderLayout(0, 0));
        norte.setOpaque(false);
        norte.add(UI.encabezado("Préstamos y devoluciones",
                "La fecha de vencimiento se calcula sola. Los préstamos se registran en un hilo en segundo plano."),
                BorderLayout.NORTH);
        norte.add(crearFormularioPrestamo(), BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);
        add(crearListado(), BorderLayout.CENTER);

        spnDias.addChangeListener(e -> actualizarVencimiento());
        btnRegistrar.addActionListener(e -> registrarPrestamo());
        cmbFiltro.addActionListener(e -> cargarTabla());
        actualizarVencimiento();
    }

    private JPanel crearFormularioPrestamo() {
        JPanel card = UI.tarjeta("Registrar préstamo");
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        cmbEstudiante.setPreferredSize(new Dimension(260, 28));
        cmbLibro.setPreferredSize(new Dimension(330, 28));
        spnDias.setPreferredSize(new Dimension(60, 28));
        lblVence.setFont(UI.FUENTE_NEGRITA);
        lblVence.setForeground(UI.PRIMARIO);

        c.gridy = 0;
        c.gridx = 0;
        card.add(etiqueta("Estudiante"), c);
        c.gridx = 1;
        card.add(etiqueta("Libro disponible"), c);
        c.gridx = 2;
        card.add(etiqueta("Días"), c);
        c.gridx = 3;
        card.add(etiqueta("Vence el"), c);

        c.gridy = 1;
        c.gridx = 0;
        c.weightx = 0.4;
        card.add(cmbEstudiante, c);
        c.gridx = 1;
        c.weightx = 0.6;
        card.add(cmbLibro, c);
        c.gridx = 2;
        c.weightx = 0;
        card.add(spnDias, c);
        c.gridx = 3;
        card.add(lblVence, c);
        c.gridx = 4;
        card.add(btnRegistrar, c);

        c.gridy = 2;
        c.gridx = 0;
        c.gridwidth = 5;
        lblResultado.setFont(UI.FUENTE_NEGRITA);
        card.add(lblResultado, c);
        return card;
    }

    private JPanel crearListado() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setOpaque(false);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        barra.setOpaque(false);
        JButton btnDevolver = UI.boton("Registrar devolución", UI.EXITO);
        JButton btnConstancia = UI.boton("Dejar constancia de atraso", UI.ADVERTENCIA);
        JButton btnPrueba = UI.botonPrimario("Prueba de concurrencia...");
        JButton btnActualizar = UI.botonSecundario("Actualizar");
        barra.add(new JLabel("Mostrar:"));
        barra.add(cmbFiltro);
        barra.add(btnDevolver);
        barra.add(btnConstancia);
        barra.add(btnPrueba);
        barra.add(btnActualizar);
        card.add(barra, BorderLayout.NORTH);

        UI.anchos(tabla, 45, 170, 110, 230, 110, 110, 190);
        card.add(UI.scroll(tabla), BorderLayout.CENTER);

        btnDevolver.addActionListener(e -> registrarDevolucion());
        btnConstancia.addActionListener(e -> registrarConstancia());
        btnPrueba.addActionListener(e -> new PruebaConcurrenciaDialog(
                SwingUtilities.getWindowAncestor(this), controller, this::refrescar).setVisible(true));
        btnActualizar.addActionListener(e -> refrescar());
        return card;
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(UI.FUENTE_NEGRITA);
        return l;
    }

    @Override
    public void refrescar() {
        cargarCombos();
        cargarTabla();
    }

    private void cargarCombos() {
        try {
            Estudiante estSel = (Estudiante) cmbEstudiante.getSelectedItem();
            DefaultComboBoxModel<Estudiante> me = new DefaultComboBoxModel<>();
            for (Estudiante e : controller.listarEstudiantes()) {
                me.addElement(e);
            }
            cmbEstudiante.setModel(me);
            if (estSel != null) {
                for (int i = 0; i < me.getSize(); i++) {
                    if (me.getElementAt(i).getId() == estSel.getId()) {
                        cmbEstudiante.setSelectedIndex(i);
                    }
                }
            }
            DefaultComboBoxModel<Libro> ml = new DefaultComboBoxModel<>();
            for (Libro l : controller.listarLibrosDisponibles()) {
                ml.addElement(l);
            }
            cmbLibro.setModel(ml);
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
    }

    private void cargarTabla() {
        try {
            prestamos = FILTRO_TODOS.equals(cmbFiltro.getSelectedItem())
                    ? controller.listarTodos() : controller.listarActivos();
            LocalDate hoy = LocalDate.now();
            modelo.setRowCount(0);
            long atrasados = 0;
            for (Prestamo p : prestamos) {
                if (p.estaAtrasado(hoy)) {
                    atrasados++;
                }
                modelo.addRow(new Object[]{p.getId(), p.getEstudiante().getNombre(), p.getEstudiante().getCurso(),
                        p.getLibro().getTitulo(), p.getFechaPrestamo(), p.getFechaVencimiento(),
                        p.getDescripcionEstado(hoy)});
            }
            barraEstado.mensaje(prestamos.size() + " préstamo(s) listados, " + atrasados + " atrasado(s).");
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
    }

    private void actualizarVencimiento() {
        LocalDate vence = controller.calcularVencimiento((Integer) spnDias.getValue());
        lblVence.setText(vence.format(UI.FORMATO_FECHA));
    }

    private void registrarPrestamo() {
        Estudiante estudiante = (Estudiante) cmbEstudiante.getSelectedItem();
        Libro libro = (Libro) cmbLibro.getSelectedItem();
        int dias = (Integer) spnDias.getValue();
        try {
            controller.registrarPrestamoEnSegundoPlano(estudiante, libro, dias, new ResultadoListener<Prestamo>() {
                @Override
                public void alTerminar(Prestamo p) {
                    enProceso--;
                    lblResultado.setForeground(UI.EXITO);
                    lblResultado.setText("OK: Préstamo #" + p.getId() + " registrado: \"" + p.getLibro().getTitulo()
                            + "\" para " + p.getEstudiante().getNombre() + ". Vence el "
                            + p.getFechaVencimiento().format(UI.FORMATO_FECHA) + ". Stock restante: "
                            + p.getLibro().getStock() + ".");
                    barraEstado.terminarProceso("Préstamo #" + p.getId() + " registrado por un hilo en segundo plano.");
                    refrescar();
                }

                @Override
                public void alFallar(String mensaje) {
                    enProceso--;
                    lblResultado.setForeground(UI.PELIGRO);
                    lblResultado.setText("Error: " + mensaje);
                    barraEstado.terminarProceso("El préstamo no se pudo registrar.");
                    UI.error(PrestamoPanel.this, mensaje);
                    refrescar();
                }
            });
            enProceso++;
            lblResultado.setForeground(UI.TEXTO_SUAVE);
            lblResultado.setText("Procesando " + enProceso + " préstamo(s) en segundo plano... "
                    + "(puedes seguir usando la ventana)");
            barraEstado.iniciarProceso("Hilo en segundo plano: registrando préstamo, actualizando stock e historial...");
        } catch (BibliotecaException ex) {
            UI.advertencia(this, ex.getMessage());
        }
    }

    private Prestamo prestamoSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        int id = (int) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        return prestamos.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    private void registrarDevolucion() {
        Prestamo p = prestamoSeleccionado();
        if (p == null) {
            UI.advertencia(this, "Selecciona un préstamo de la tabla.");
            return;
        }
        if (!UI.confirmar(this, "¿Registrar la devolución de \"" + p.getLibro().getTitulo() + "\" ("
                + p.getEstudiante().getNombre() + ")?")) {
            return;
        }
        try {
            long atraso = controller.registrarDevolucion(p);
            if (atraso > 0) {
                UI.advertencia(this, "Devolución registrada CON ATRASO de " + atraso + " día(s).\n"
                        + "Quedó constancia en el historial del estudiante.");
            } else {
                UI.info(this, "Devolución registrada a tiempo. El stock fue repuesto.");
            }
            refrescar();
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
    }

    private void registrarConstancia() {
        Prestamo p = prestamoSeleccionado();
        if (p == null) {
            UI.advertencia(this, "Selecciona un préstamo atrasado de la tabla.");
            return;
        }
        try {
            long dias = controller.registrarConstanciaAtraso(p);
            UI.info(this, "Se dejó constancia de " + dias + " día(s) de atraso para "
                    + p.getEstudiante().getNombre() + ".");
            refrescar();
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
    }
}
