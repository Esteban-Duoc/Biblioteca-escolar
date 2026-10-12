package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.EstudianteController;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Estudiante;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EstudiantePanel extends JPanel implements Refrescable {
    private final EstudianteController controller = new EstudianteController();
    private final BarraEstado barraEstado;

    private final DefaultTableModel modelo = UI.modeloTabla("ID", "Nombre", "RUT", "Curso", "Correo", "Acceso");
    private final JTable tabla = UI.tabla(modelo);
    private final JTextField txtBuscar = new JTextField(22);

    private final JTextField txtNombre = new JTextField();
    private final JTextField txtRut = new JTextField();
    private final JTextField txtCurso = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JPasswordField txtContrasena = new JPasswordField();
    private final JLabel lblModo = new JLabel("Nuevo estudiante");
    private final JLabel lblAyudaClave = new JLabel();

    private List<Estudiante> estudiantes = new ArrayList<>();
    private Estudiante seleccionado;

    public EstudiantePanel(BarraEstado barraEstado) {
        super(new BorderLayout(16, 0));
        this.barraEstado = barraEstado;
        setBackground(UI.FONDO);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        construir();
    }

    private void construir() {
        add(UI.encabezado("Gestión de estudiantes", "Al registrar un estudiante se crea su cuenta (rol estudiante) "
                + "para que pueda iniciar sesión con su correo o RUT."), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setOpaque(false);
        JPanel buscador = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buscador.setOpaque(false);
        JButton btnBuscar = UI.botonPrimario("Buscar");
        JButton btnTodos = UI.botonSecundario("Ver todos");
        buscador.add(new JLabel("Buscar:"));
        buscador.add(txtBuscar);
        buscador.add(btnBuscar);
        buscador.add(btnTodos);
        centro.add(buscador, BorderLayout.NORTH);
        tabla.getColumnModel().getColumn(0).setMaxWidth(50);
        centro.add(UI.scroll(tabla), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JPanel form = UI.tarjeta("Datos del estudiante");
        form.setPreferredSize(new Dimension(330, 0));
        lblModo.setFont(UI.FUENTE_NEGRITA);
        lblModo.setForeground(UI.PRIMARIO_CLARO);
        lblAyudaClave.setForeground(UI.TEXTO_SUAVE);
        UI.filaCompleta(form, 0, lblModo);
        UI.filaFormulario(form, 1, "Nombre", txtNombre);
        UI.filaFormulario(form, 2, "RUT", txtRut);
        UI.filaFormulario(form, 3, "Curso", txtCurso);
        UI.filaFormulario(form, 4, "Correo", txtCorreo);
        UI.filaFormulario(form, 5, "Contraseña", txtContrasena);
        UI.filaCompleta(form, 6, lblAyudaClave);

        JButton btnNuevo = UI.botonSecundario("Nuevo");
        JButton btnGuardar = UI.botonPrimario("Guardar");
        JButton btnEliminar = UI.botonPeligro("Eliminar");
        JPanel botones = new JPanel(new GridLayout(1, 3, 6, 0));
        botones.setOpaque(false);
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnEliminar);
        UI.filaCompleta(form, 7, botones);
        JLabel formato = new JLabel("<html><small>RUT sin puntos y con guion: 12345678-9<br>"
                + "Curso: ej. 3ro Medio A</small></html>");
        formato.setForeground(UI.TEXTO_SUAVE);
        UI.filaCompleta(form, 8, formato);
        UI.relleno(form, 9);
        add(form, BorderLayout.EAST);

        btnBuscar.addActionListener(e -> cargarTabla());
        txtBuscar.addActionListener(e -> cargarTabla());
        btnTodos.addActionListener(e -> {
            txtBuscar.setText("");
            cargarTabla();
        });
        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFila();
            }
        });
        limpiar();
    }

    @Override
    public void refrescar() {
        cargarTabla();
    }

    private void cargarTabla() {
        try {
            estudiantes = controller.buscar(txtBuscar.getText());
            Set<String> conCuenta = controller.rutsConCuenta();
            modelo.setRowCount(0);
            for (Estudiante e : estudiantes) {
                modelo.addRow(new Object[]{e.getId(), e.getNombre(), e.getRut(), e.getCurso(), e.getCorreo(),
                        conCuenta.contains(e.getRut()) ? "Sí" : "No"});
            }
            barraEstado.mensaje(estudiantes.size() + " estudiante(s) encontrados.");
            limpiar();
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        int id = (int) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        seleccionado = estudiantes.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
        if (seleccionado == null) {
            return;
        }
        txtNombre.setText(seleccionado.getNombre());
        txtRut.setText(seleccionado.getRut());
        txtCurso.setText(seleccionado.getCurso());
        txtCorreo.setText(seleccionado.getCorreo());
        txtContrasena.setText("");
        lblModo.setText("Editando estudiante #" + id);
        lblAyudaClave.setText("<html><small>Deja la contraseña vacía para no cambiarla.</small></html>");
    }

    private void limpiar() {
        seleccionado = null;
        tabla.clearSelection();
        txtNombre.setText("");
        txtRut.setText("");
        txtCurso.setText("");
        txtCorreo.setText("");
        txtContrasena.setText("");
        lblModo.setText("Nuevo estudiante");
        lblAyudaClave.setText("<html><small>Contraseña con la que el estudiante iniciará sesión.</small></html>");
    }

    private void guardar() {
        Estudiante e = new Estudiante();
        e.setId(seleccionado == null ? 0 : seleccionado.getId());
        e.setNombre(txtNombre.getText());
        e.setRut(txtRut.getText());
        e.setCurso(txtCurso.getText());
        e.setCorreo(txtCorreo.getText());
        String rutAnterior = seleccionado == null ? null : seleccionado.getRut();
        boolean esNuevo = seleccionado == null;
        try {
            controller.guardar(e, new String(txtContrasena.getPassword()), rutAnterior);
            UI.info(this, esNuevo ? "Estudiante registrado. Ya puede iniciar sesión con su correo o RUT."
                    : "Estudiante actualizado.");
            cargarTabla();
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
    }

    private void eliminar() {
        if (seleccionado == null) {
            UI.advertencia(this, "Selecciona un estudiante de la tabla.");
            return;
        }
        if (!UI.confirmar(this, "¿Eliminar a " + seleccionado.getNombre() + " y su cuenta de acceso?")) {
            return;
        }
        try {
            controller.eliminar(seleccionado);
            UI.info(this, "Estudiante eliminado.");
            cargarTabla();
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
    }
}
