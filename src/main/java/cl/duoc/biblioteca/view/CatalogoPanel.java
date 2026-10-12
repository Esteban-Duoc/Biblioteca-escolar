package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.LibroController;
import cl.duoc.biblioteca.controller.PrestamoController;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.model.Prestamo;
import cl.duoc.biblioteca.service.ResultadoListener;
import cl.duoc.biblioteca.service.Sesion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

public class CatalogoPanel extends JPanel implements Refrescable {
    private final LibroController libroController = new LibroController();
    private final PrestamoController prestamoController = new PrestamoController();
    private final BarraEstado barraEstado;

    private final DefaultTableModel modelo = UI.modeloTabla("ID", "Título", "Autor", "Editorial", "Categoría", "Disponibles");
    private final JTable tabla = UI.tabla(modelo);
    private final JTextField txtBuscar = new JTextField(24);
    private final JButton btnSolicitar = UI.botonPrimario("Solicitar préstamo (" + Prestamo.DIAS_PRESTAMO_POR_DEFECTO + " días)");
    private final JLabel lblResultado = new JLabel(" ");

    private List<Libro> libros = new ArrayList<>();

    public CatalogoPanel(BarraEstado barraEstado) {
        super(new BorderLayout(0, 10));
        this.barraEstado = barraEstado;
        setBackground(UI.FONDO);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        construir();
    }

    private void construir() {
        JPanel norte = new JPanel(new BorderLayout(0, 6));
        norte.setOpaque(false);
        norte.add(UI.encabezado("Catálogo de libros",
                "Busca un libro, selecciónalo y solicita el préstamo. Debes devolverlo dentro de "
                        + Prestamo.DIAS_PRESTAMO_POR_DEFECTO + " días."), BorderLayout.NORTH);
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        barra.setOpaque(false);
        JButton btnBuscar = UI.botonSecundario("Buscar");
        barra.add(new JLabel("Buscar por título, autor o categoría:"));
        barra.add(txtBuscar);
        barra.add(btnBuscar);
        barra.add(btnSolicitar);
        norte.add(barra, BorderLayout.CENTER);
        lblResultado.setFont(UI.FUENTE_NEGRITA);
        norte.add(lblResultado, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        tabla.getColumnModel().getColumn(0).setMaxWidth(50);
        add(UI.scroll(tabla), BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> refrescar());
        txtBuscar.addActionListener(e -> refrescar());
        btnSolicitar.addActionListener(e -> solicitar());
    }

    @Override
    public void refrescar() {
        try {
            libros = libroController.buscar(txtBuscar.getText());
            modelo.setRowCount(0);
            for (Libro l : libros) {
                modelo.addRow(new Object[]{l.getId(), l.getTitulo(), l.getAutor(), l.getEditorial(),
                        l.getCategoria() == null ? "-" : l.getCategoria().getNombre(),
                        l.hayStock() ? String.valueOf(l.getStock()) : "Sin stock"});
            }
            barraEstado.mensaje(libros.size() + " libro(s) en el catálogo.");
        } catch (BibliotecaException ex) {
            UI.error(this, ex.getMessage());
        }
    }

    private void solicitar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            UI.advertencia(this, "Selecciona un libro del catálogo.");
            return;
        }
        int id = (int) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        Libro libro = libros.stream().filter(l -> l.getId() == id).findFirst().orElse(null);
        Estudiante yo = Sesion.getInstance().getEstudiante();
        try {
            btnSolicitar.setEnabled(false);
            prestamoController.registrarPrestamoEnSegundoPlano(yo, libro, Prestamo.DIAS_PRESTAMO_POR_DEFECTO,
                    new ResultadoListener<Prestamo>() {
                        @Override
                        public void alTerminar(Prestamo p) {
                            btnSolicitar.setEnabled(true);
                            lblResultado.setForeground(UI.EXITO);
                            lblResultado.setText("Préstamo #" + p.getId() + " registrado: \"" + p.getLibro().getTitulo()
                                    + "\". Devuélvelo a más tardar el "
                                    + p.getFechaVencimiento().format(UI.FORMATO_FECHA) + ".");
                            barraEstado.terminarProceso("Préstamo registrado en segundo plano.");
                            refrescar();
                        }

                        @Override
                        public void alFallar(String mensaje) {
                            btnSolicitar.setEnabled(true);
                            lblResultado.setForeground(UI.PELIGRO);
                            lblResultado.setText(mensaje);
                            barraEstado.terminarProceso("No se pudo registrar el préstamo.");
                            UI.error(CatalogoPanel.this, mensaje);
                            refrescar();
                        }
                    });
            lblResultado.setForeground(UI.TEXTO_SUAVE);
            lblResultado.setText("Procesando tu préstamo en segundo plano...");
            barraEstado.iniciarProceso("Hilo en segundo plano: registrando préstamo...");
        } catch (BibliotecaException ex) {
            btnSolicitar.setEnabled(true);
            UI.advertencia(this, ex.getMessage());
        }
    }
}
