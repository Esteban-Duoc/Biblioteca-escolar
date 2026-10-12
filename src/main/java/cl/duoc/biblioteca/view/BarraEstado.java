package cl.duoc.biblioteca.view;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class BarraEstado extends JPanel {
    private final JLabel mensaje = new JLabel("Listo.");
    private final JProgressBar progreso = new JProgressBar();
    private final JLabel reloj = new JLabel();
    private int procesosActivos = 0;

    public BarraEstado() {
        super(new BorderLayout());
        setBackground(UI.BLANCO);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UI.BORDE),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        mensaje.setForeground(UI.TEXTO_SUAVE);
        progreso.setIndeterminate(true);
        progreso.setVisible(false);
        progreso.setPreferredSize(new Dimension(160, 14));
        reloj.setFont(UI.FUENTE_NEGRITA);
        reloj.setForeground(UI.PRIMARIO);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        derecha.setOpaque(false);
        derecha.add(progreso);
        derecha.add(reloj);
        add(mensaje, BorderLayout.CENTER);
        add(derecha, BorderLayout.EAST);

        DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss");
        Timer timer = new Timer(1000, e -> reloj.setText(LocalTime.now().format(f)));
        timer.setInitialDelay(0);
        timer.start();
    }

    public void mensaje(String texto) {
        mensaje.setText(texto);
    }

    public void iniciarProceso(String texto) {
        procesosActivos++;
        mensaje.setText(texto);
        progreso.setVisible(true);
    }

    public void terminarProceso(String texto) {
        procesosActivos = Math.max(0, procesosActivos - 1);
        mensaje.setText(texto);
        progreso.setVisible(procesosActivos > 0);
    }
}
