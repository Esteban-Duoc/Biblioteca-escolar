package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.LibroController;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Categoria;
import cl.duoc.biblioteca.model.Libro;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class LibroPanel extends JPanel implements Refrescable {
    private final LibroController controller = new LibroController();
    private final BarraEstado barraEstado;

    private final DefaultTableModel modelo = UI.modeloTabla("ID", "Título", "Autor", "ISBN", "Editorial", "Categoría", "Stock");
    private final JTable tabla = UI.tabla(modelo);
    private final JTextField txtBuscar = new JTextField(22);

    private final JTextField txtTitulo = new JTextField();
    private final JTextField txtAutor = new JTextField();
    private final JTextField txtIsbn = new JTextField();
    private final JTextField txtEditorial = new JTextField();
    private final JSpinner spnStock = new JSpinner(new SpinnerNumberModel(1, 0, 9999, 1));
    private final JComboBox<Categoria> cmbCategoria = new JComboBox<>();
    private final JLabel lblModo = new JLabel("Nuevo libro");

    private List<Libro> libros = new ArrayList<>();
    private Libro seleccionado;

    public LibroPanel(BarraEstado barraEstado) {
        super(new BorderLayout(16, 0));
        this.barraEstado = barraEstado;
        setBackground(UI.FONDO);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        construir();
    }

    private void construir() {
        add(UI.encabezado("Gestión de libros", "Registra, modifica, elimina y consulta el inventario. "
                + "Selecciona una fila para editarla."), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setOpaque(false);
        JPanel buscador = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buscador.setOpaque(false);
        JButton btnBuscar = UI.botonPrimario("Buscar");
        JButton btnLimpiarBusqueda = UI.botonSecundario("Ver todos");
        buscador.add(new JLabel("Buscar:"));
        buscador.add(txtBuscar);
        buscador.add(btnBuscar);
        buscador.add(btnLimpiarBusqueda);
        centro.add(buscador, BorderLayout.NORTH);
        UI.anchos(tabla, 40, 230, 150, 120, 120, 120, 55);
        centro.add(UI.scroll(tabla), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JPanel form = UI.tarjeta("Datos del libro");
        form.setPreferredSize(new Dimension(330, 0));
        lblModo.setFont(UI.FUENTE_NEGRITA);
        lblModo.setForeground(UI.PRIMARIO_CLARO);
        UI.filaCompleta(form, 0, lblModo);
        UI.filaFormulario(form, 1, "Título", txtTitulo);
        UI.filaFormulario(form, 2, "Autor", txtAutor);
        UI.filaFormulario(form, 3, "ISBN", txtIsbn);
        UI.filaFormulario(form, 4, "Editorial", txtEditorial);
        UI.filaFormulario(form, 5, "Categoría", cmbCategoria);
        UI.filaFormulario(form, 6, "Stock", spnStock);

        JButton btnNuevo = UI.botonSecundario("Nuevo");
        JButton btnGuardar = UI.botonPrimario("Guardar");
        JButton btnEliminar = UI.botonPeligro("Eliminar");
        JPanel botones = new JPanel(new GridLayout(1, 3, 6, 0));
        botones.setOpaque(false);
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnEliminar);
        UI.filaCompleta(form, 7, botones);
        JLabel nota = new JLabel("<html><small>El stock indica las copias disponibles para préstamo. "
                + "Se descuenta y repone automáticamente al prestar y devolver.</small></html>");
        nota.setForeground(UI.TEXTO_SUAVE);
        UI.filaCompleta(form, 8, nota);
        UI.relleno(form, 9);
        add(form, BorderLayout.EAST);

        btnBuscar.addActionListener(e -> cargarTabla());
        txtBuscar.addActionListener(e -> cargarTabla());
        btnLimpiarBusqueda.addActionListener(e -> {
            txtBuscar.setText("");
            cargarTabla();
        });
        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFila();
            }
        });
    }

    @Override
    public void refrescar() {
        cargarCategorias();
        cargarTabla();
    }

    private void cargarCategorias() {
        try {
            DefaultComboBoxModel<Categoria> m = new DefaultComboBoxModel<>();
            for (Categoria c : controller.listarCategorias()) {
                m.addElement(c);
            }
            cmbCategoria.setModel(m);
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
    }

    private void cargarTabla() {
        try {
            libros = controller.buscar(txtBuscar.getText());
            modelo.setRowCount(0);
            for (Libro l : libros) {
                modelo.addRow(new Object[]{l.getId(), l.getTitulo(), l.getAutor(), l.getIsbn(), l.getEditorial(),
                        l.getCategoria() == null ? "-" : l.getCategoria().getNombre(), l.getStock()});
            }
            barraEstado.mensaje(libros.size() + " libro(s) encontrados.");
            limpiarFormulario();
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
        seleccionado = libros.stream().filter(l -> l.getId() == id).findFirst().orElse(null);
        if (seleccionado == null) {
            return;
        }
        txtTitulo.setText(seleccionado.getTitulo());
        txtAutor.setText(seleccionado.getAutor());
        txtIsbn.setText(seleccionado.getIsbn());
        txtEditorial.setText(seleccionado.getEditorial());
        spnStock.setValue(seleccionado.getStock());
        cmbCategoria.setSelectedItem(seleccionado.getCategoria());
        lblModo.setText("Editando libro #" + seleccionado.getId());
    }

    private void limpiarFormulario() {
        seleccionado = null;
        tabla.clearSelection();
        txtTitulo.setText("");
        txtAutor.setText("");
        txtIsbn.setText("");
        txtEditorial.setText("");
        spnStock.setValue(1);
        if (cmbCategoria.getItemCount() > 0) {
            cmbCategoria.setSelectedIndex(0);
        }
        lblModo.setText("Nuevo libro");
    }

    private void guardar() {
        Libro libro = new Libro();
        libro.setId(seleccionado == null ? 0 : seleccionado.getId());
        libro.setTitulo(txtTitulo.getText());
        libro.setAutor(txtAutor.getText());
        libro.setIsbn(txtIsbn.getText());
        libro.setEditorial(txtEditorial.getText());
        libro.setStock((Integer) spnStock.getValue());
        libro.setCategoria((Categoria) cmbCategoria.getSelectedItem());
        boolean esNuevo = libro.getId() == 0;
        try {
            controller.guardar(libro);
            UI.info(this, esNuevo ? "Libro registrado correctamente." : "Libro actualizado correctamente.");
            cargarTabla();
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
    }

    private void eliminar() {
        if (seleccionado == null) {
            UI.advertencia(this, "Selecciona un libro de la tabla.");
            return;
        }
        if (!UI.confirmar(this, "¿Eliminar el libro \"" + seleccionado.getTitulo() + "\"?")) {
            return;
        }
        try {
            controller.eliminar(seleccionado);
            UI.info(this, "Libro eliminado.");
            cargarTabla();
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
    }
}
