package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.PrestamoController;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.service.PruebaConcurrencia;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.util.List;

public class PruebaConcurrenciaDialog extends JDialog {
    private final PrestamoController controller;
    private final Runnable alCerrar;

    private final JComboBox<Libro> cmbLibro = new JComboBox<>();
    private final JSpinner spnHilos = new JSpinner(new SpinnerNumberModel(8, 2, 20, 1));
    private final JCheckBox chkRevertir = new JCheckBox("Revertir los préstamos de prueba al terminar", true);
    private final JTextArea txtLog = new JTextArea();
    private final JLabel lblResultado = new JLabel(" ");
    private final JProgressBar progreso = new JProgressBar();
    private final JButton btnEjecutar = UI.botonPrimario("Ejecutar prueba");

    public PruebaConcurrenciaDialog(Window padre, PrestamoController controller, Runnable alCerrar) {
        super(padre, "Prueba de préstamos simultáneos", ModalityType.MODELESS);
        this.controller = controller;
        this.alCerrar = alCerrar;
        setSize(1000, 560);
        setLocationRelativeTo(padre);
        construir();
        cargarLibros();
    }

    private void construir() {
        JPanel raiz = new JPanel(new BorderLayout(0, 10));
        raiz.setBackground(UI.FONDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        raiz.add(UI.encabezado("Prueba de concurrencia", ""), BorderLayout.NORTH);

        JPanel opciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        opciones.setOpaque(false);
        cmbLibro.setPreferredSize(new Dimension(320, 28));
        opciones.add(new JLabel("Libro:"));
        opciones.add(cmbLibro);
        opciones.add(new JLabel("Hilos:"));
        opciones.add(spnHilos);
        opciones.add(chkRevertir);
        opciones.add(btnEjecutar);
        chkRevertir.setOpaque(false);

        txtLog.setEditable(false);
        txtLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setOpaque(false);
        centro.add(opciones, BorderLayout.NORTH);
        centro.add(UI.scroll(txtLog), BorderLayout.CENTER);
        raiz.add(centro, BorderLayout.CENTER);

        JPanel sur = new JPanel(new BorderLayout(10, 0));
        sur.setOpaque(false);
        lblResultado.setFont(UI.FUENTE_NEGRITA);
        progreso.setIndeterminate(true);
        progreso.setVisible(false);
        sur.add(lblResultado, BorderLayout.CENTER);
        sur.add(progreso, BorderLayout.EAST);
        raiz.add(sur, BorderLayout.SOUTH);
        setContentPane(raiz);

        btnEjecutar.addActionListener(e -> ejecutar());
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                alCerrar.run();
            }
        });
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void cargarLibros() {
        try {
            DefaultComboBoxModel<Libro> m = new DefaultComboBoxModel<>();
            for (Libro l : controller.listarLibros()) {
                m.addElement(l);
            }
            cmbLibro.setModel(m);
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
    }

    private void ejecutar() {
        Libro libro = (Libro) cmbLibro.getSelectedItem();
        if (libro == null) {
            UI.advertencia(this, "Selecciona un libro.");
            return;
        }
        int hilos = (Integer) spnHilos.getValue();
        try {
            List<Estudiante> estudiantes = controller.estudiantesParaPrueba(libro, hilos);
            if (estudiantes.size() < hilos) {
                txtLog.append("Aviso: solo hay " + estudiantes.size()
                        + " estudiantes disponibles para este libro; se usarán " + estudiantes.size() + " hilos.\n");
            }
            btnEjecutar.setEnabled(false);
            progreso.setVisible(true);
            lblResultado.setForeground(UI.TEXTO_SUAVE);
            lblResultado.setText("Ejecutando...");
            txtLog.append("================ NUEVA PRUEBA ================\n");

            controller.ejecutarPruebaConcurrencia(libro, estudiantes, chkRevertir.isSelected(),
                    new PruebaConcurrencia.Observador() {
                        @Override
                        public void registrar(String linea) {
                            txtLog.append(linea + "\n");
                            txtLog.setCaretPosition(txtLog.getDocument().getLength());
                        }

                        @Override
                        public void terminado(PruebaConcurrencia.Resultado r) {
                            btnEjecutar.setEnabled(true);
                            progreso.setVisible(false);
                            cargarLibros();
                            if (r == null) {
                                lblResultado.setForeground(UI.PELIGRO);
                                lblResultado.setText("La prueba terminó con errores (revisa el registro).");
                            } else if (r.consistente) {
                                lblResultado.setForeground(UI.EXITO);
                                lblResultado.setText("Stock consistente: inicial " + r.stockInicial + " - aceptados "
                                        + r.aceptados + " = final " + r.stockFinal + ". Rechazados: " + r.rechazados + ".");
                            } else {
                                lblResultado.setForeground(UI.PELIGRO);
                                lblResultado.setText("Inconsistencia: stock final " + r.stockFinal + ".");
                            }
                        }
                    });
        } catch (BibliotecaException ex) {
            btnEjecutar.setEnabled(true);
            progreso.setVisible(false);
            UI.error(this, ex.getMessage());
        }
    }
}
