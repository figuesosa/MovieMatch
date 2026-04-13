package moviematch.vista;

import moviematch.modelo.Pelicula;
import moviematch.modelo.Usuario;
import moviematch.servicio.ServicioRecomendacion;
import moviematch.servicio.ServicioRecomendacion.RecomendacionExplicada;
import moviematch.util.CachePosters;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

/**
 * Lista de recomendaciones para el usuario actual (contenido de la pestaña).
 */
public class PanelRecomendaciones extends JPanel {

    private final Usuario usuario;
    private final ServicioRecomendacion servicio = new ServicioRecomendacion();
    private final DefaultListModel<String> modeloLista = new DefaultListModel<>();
    private final JList<String> lista = new JList<>(modeloLista);
    private final List<Pelicula> recomendacionesActuales = new ArrayList<>();
    private final JLabel etiquetaPoster = new JLabel("Selecciona una pelicula", SwingConstants.CENTER);

    public PanelRecomendaciones(Usuario usuario) {
        super(new BorderLayout());
        this.usuario = usuario;

        JLabel titulo = new JLabel(
                "<html>Películas sugeridas (popularidad y media de valoraciones). "
                        + "Usa <b>Géneros preferidos</b>, <b>Calificaciones</b> y <b>Mi lista</b>; luego pulsa Actualizar.</html>");
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        lista.setVisibleRowCount(14);
        JScrollPane scroll = new JScrollPane(lista);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        lista.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarPosterSeleccionado();
            }
        });
        etiquetaPoster.setPreferredSize(new Dimension(180, 250));
        etiquetaPoster.setBorder(BorderFactory.createTitledBorder("Poster"));
        JPanel centro = new JPanel(new BorderLayout(8, 0));
        centro.add(scroll, BorderLayout.CENTER);
        centro.add(etiquetaPoster, BorderLayout.EAST);

        JButton actualizar = new JButton("Actualizar lista");
        actualizar.addActionListener(e -> cargarRecomendaciones());

        JPanel sur = new JPanel();
        sur.add(actualizar);

        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(titulo, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);

        cargarRecomendaciones();
    }

    public void cargarRecomendaciones() {
        modeloLista.clear();
        recomendacionesActuales.clear();
        etiquetaPoster.setIcon(null);
        etiquetaPoster.setText("Selecciona una pelicula");
        try {
            List<RecomendacionExplicada> rec = servicio.obtenerRecomendacionesExplicadasParaUsuario(usuario.getId());
            if (rec.isEmpty()) {
                modeloLista.addElement(
                        "(No hay recomendaciones: revisa géneros preferidos o que queden películas sin calificar ni en tu lista.)");
                return;
            }
            for (RecomendacionExplicada r : rec) {
                Pelicula p = r.pelicula();
                recomendacionesActuales.add(p);
                String anio = p.getAnioLanzamiento() > 0
                        ? String.valueOf(p.getAnioLanzamiento())
                        : "s/d";
                modeloLista.addElement(String.format("%s — año %s · %s",
                        p.getTitulo(),
                        anio,
                        r.motivo()));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al consultar la base de datos:\n" + ex.getMessage(),
                    "MovieMatch",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarPosterSeleccionado() {
        int idx = lista.getSelectedIndex();
        if (idx < 0 || idx >= recomendacionesActuales.size()) {
            etiquetaPoster.setIcon(null);
            etiquetaPoster.setText("Selecciona una pelicula");
            return;
        }
        Pelicula p = recomendacionesActuales.get(idx);
        etiquetaPoster.setIcon(null);
        etiquetaPoster.setText("Cargando...");
        new SwingWorker<javax.swing.ImageIcon, Void>() {
            @Override
            protected javax.swing.ImageIcon doInBackground() {
                return CachePosters.get().obtenerPosterEscalado(p, 170, 240);
            }

            @Override
            protected void done() {
                try {
                    javax.swing.ImageIcon icono = get();
                    if (icono != null) {
                        etiquetaPoster.setText("");
                        etiquetaPoster.setIcon(icono);
                    } else {
                        etiquetaPoster.setIcon(null);
                        etiquetaPoster.setText("Sin poster");
                    }
                } catch (Exception ignored) {
                    etiquetaPoster.setIcon(null);
                    etiquetaPoster.setText("Sin poster");
                }
            }
        }.execute();
    }
}
