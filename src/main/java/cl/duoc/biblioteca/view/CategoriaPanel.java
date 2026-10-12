package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.CategoriaController;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Categoria;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class CategoriaPanel extends JPanel implements Refrescable {
    private final CategoriaController controller = new CategoriaController();
    private final BarraEstado barraEstado;

    private final DefaultTableModel modelo = UI.modeloTabla("ID", "Nombre");
    private final JTable tabla = UI.tabla(modelo);
    private final JTextField txtNombre = new JTextField();
    private final JLabel lblModo = new JLabel("Nueva categoría");

    private List<Categoria> categorias = new ArrayList<>();
    private Categoria seleccionada;

    public CategoriaPanel(BarraEstado barraEstado) {
        super(new BorderLayout(16, 0));
        this.barraEstado = barraEstado;
        setBackground(UI.FONDO);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        construir();
    }

    private void construir() {
        add(UI.encabezado("Categorías", "Cada libro pertenece a una categoría."), BorderLayout.NORTH);
        tabla.getColumnModel().getColumn(0).setMaxWidth(60);
        add(UI.scroll(tabla), BorderLayout.CENTER);

        JPanel form = UI.tarjeta("Datos de la categoría");
        form.setPreferredSize(new Dimension(330, 0));
        lblModo.setFont(UI.FUENTE_NEGRITA);
        lblModo.setForeground(UI.PRIMARIO_CLARO);
        UI.filaCompleta(form, 0, lblModo);
        UI.filaFormulario(form, 1, "Nombre", txtNombre);
        JButton btnNuevo = UI.botonSecundario("Nuevo");
        JButton btnGuardar = UI.botonPrimario("Guardar");
        JButton btnEliminar = UI.botonPeligro("Eliminar");
        JPanel botones = new JPanel(new GridLayout(1, 3, 6, 0));
        botones.setOpaque(false);
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnEliminar);
        UI.filaCompleta(form, 2, botones);
        UI.relleno(form, 3);
        add(form, BorderLayout.EAST);

        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        txtNombre.addActionListener(e -> guardar());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFila();
            }
        });
    }

    @Override
    public void refrescar() {
        try {
            categorias = controller.listar();
            modelo.setRowCount(0);
            for (Categoria c : categorias) {
                modelo.addRow(new Object[]{c.getId(), c.getNombre()});
            }
            barraEstado.mensaje(categorias.size() + " categoría(s).");
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
        seleccionada = categorias.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
        if (seleccionada != null) {
            txtNombre.setText(seleccionada.getNombre());
            lblModo.setText("Editando categoría #" + id);
        }
    }

    private void limpiar() {
        seleccionada = null;
        tabla.clearSelection();
        txtNombre.setText("");
        lblModo.setText("Nueva categoría");
    }

    private void guardar() {
        Categoria c = new Categoria(seleccionada == null ? 0 : seleccionada.getId(), null);
        c.setNombre(txtNombre.getText());
        try {
            controller.guardar(c);
            UI.info(this, "Categoría guardada.");
            refrescar();
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
    }

    private void eliminar() {
        if (seleccionada == null) {
            UI.advertencia(this, "Selecciona una categoría de la tabla.");
            return;
        }
        if (!UI.confirmar(this, "¿Eliminar la categoría \"" + seleccionada.getNombre() + "\"?")) {
            return;
        }
        try {
            controller.eliminar(seleccionada);
            UI.info(this, "Categoría eliminada.");
            refrescar();
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
    }
}
