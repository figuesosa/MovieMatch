package moviematch.vista;

import moviematch.dao.CalificacionDAO;
import moviematch.dao.PeliculaDAO;
import moviematch.modelo.Calificacion;
import moviematch.modelo.Pelicula;
import moviematch.modelo.Usuario;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD de calificaciones del usuario ({@code ratings}).
 */
public class PanelCalificaciones extends JPanel {

    private final Usuario usuario;
    private final Runnable alCambiarDatos;
    private final CalificacionDAO calificacionDAO = new CalificacionDAO();
    private final PeliculaDAO peliculaDAO = new PeliculaDAO();

    private final DefaultListModel<String> modeloLista = new DefaultListModel<>();
    private final JList<String> lista = new JList<>(modeloLista);
    private final List<CalificacionDAO.CalificacionVista> filas = new ArrayList<>();

    private final JLabel etiquetaPelicula = new JLabel("—");
    private final JComboBox<Pelicula> comboPelicula = new JComboBox<>();
    private final JSpinner spinnerPuntos = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
    private final JTextArea areaResena = new JTextArea(3, 28);

    private int ratingIdEdicion = 0;

    public PanelCalificaciones(Usuario usuario, Runnable alCambiarDatos) {
        super(new BorderLayout());
        this.usuario = usuario;
        this.alCambiarDatos = alCambiarDatos;

        JLabel ayuda = new JLabel(
                "<html>Tus valoraciones (1–5). Una película solo puede tener <b>una</b> calificación por usuario.</html>");
        ayuda.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                sincronizarFormularioDesdeLista();
            }
        });
        JScrollPane scrollLista = new JScrollPane(lista);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 4, 3, 4);
        g.anchor = GridBagConstraints.LINE_START;
        g.gridx = 0;
        g.gridy = 0;
        form.add(new JLabel("Película (nueva):"), g);
        g.gridx = 1;
        form.add(comboPelicula, g);
        g.gridx = 0;
        g.gridy = 1;
        form.add(new JLabel("Seleccionada:"), g);
        g.gridx = 1;
        form.add(etiquetaPelicula, g);
        g.gridx = 0;
        g.gridy = 2;
        form.add(new JLabel("Puntuación:"), g);
        g.gridx = 1;
        form.add(spinnerPuntos, g);
        g.gridx = 0;
        g.gridy = 3;
        form.add(new JLabel("Reseña (opcional):"), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.BOTH;
        g.weightx = 1;
        form.add(new JScrollPane(areaResena), g);

        JButton nueva = new JButton("Nueva calificación");
        nueva.addActionListener(e -> prepararNueva());
        JButton guardar = new JButton("Guardar");
        guardar.addActionListener(e -> guardar());
        JButton eliminar = new JButton("Eliminar");
        eliminar.addActionListener(e -> eliminarSeleccionada());
        JButton refrescar = new JButton("Actualizar lista");
        refrescar.addActionListener(e -> recargar());

        JPanel botones = new JPanel();
        botones.add(nueva);
        botones.add(guardar);
        botones.add(eliminar);
        botones.add(refrescar);

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.add(form, BorderLayout.NORTH);
        derecha.add(botones, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout(8, 0));
        centro.add(scrollLista, BorderLayout.CENTER);
        centro.add(derecha, BorderLayout.EAST);

        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(ayuda, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);

        rellenarComboPeliculas();
        recargar();
    }

    public void recargar() {
        modeloLista.clear();
        filas.clear();
        for (CalificacionDAO.CalificacionVista c : calificacionDAO.obtenerCalificacionesVistaPorUsuario(usuario.getId())) {
            filas.add(c);
            String r = c.resena() == null || c.resena().isBlank() ? "—" : c.resena();
            if (r.length() > 42) {
                r = r.substring(0, 42) + "…";
            }
            modeloLista.addElement(String.format("[%d★] %s — %s", c.puntuacion(), c.tituloPelicula(), r));
        }
        rellenarComboPeliculas();
        prepararNueva();
    }

    private void rellenarComboPeliculas() {
        comboPelicula.removeAllItems();
        for (Pelicula p : peliculaDAO.obtenerTodasLasPeliculas()) {
            comboPelicula.addItem(p);
        }
    }

    private void sincronizarFormularioDesdeLista() {
        int i = lista.getSelectedIndex();
        if (i < 0 || i >= filas.size()) {
            return;
        }
        CalificacionDAO.CalificacionVista c = filas.get(i);
        ratingIdEdicion = c.ratingId();
        etiquetaPelicula.setText(c.tituloPelicula());
        comboPelicula.setEnabled(false);
        spinnerPuntos.setValue(c.puntuacion());
        areaResena.setText(c.resena() != null ? c.resena() : "");
    }

    private void prepararNueva() {
        lista.clearSelection();
        ratingIdEdicion = 0;
        etiquetaPelicula.setText("(elige película a la derecha)");
        comboPelicula.setEnabled(true);
        spinnerPuntos.setValue(3);
        areaResena.setText("");
    }

    private void guardar() {
        int puntos = (Integer) spinnerPuntos.getValue();
        String resenaTxt = areaResena.getText().trim();
        String resenaSql = resenaTxt.isEmpty() ? null : resenaTxt;

        if (ratingIdEdicion > 0) {
            Calificacion cal = new Calificacion();
            cal.setId(ratingIdEdicion);
            cal.setUsuarioId(usuario.getId());
            cal.setPuntuacion(puntos);
            cal.setResena(resenaSql);
            if (!calificacionDAO.actualizarCalificacion(cal)) {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar la calificación.", "MovieMatch", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            Pelicula p = (Pelicula) comboPelicula.getSelectedItem();
            if (p == null) {
                JOptionPane.showMessageDialog(this, "Selecciona una película.", "MovieMatch", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (calificacionDAO.obtenerPorUsuarioYPelicula(usuario.getId(), p.getId()) != null) {
                JOptionPane.showMessageDialog(this,
                        "Ya calificaste esa película. Elígela en la lista para editarla.",
                        "MovieMatch",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Calificacion cal = new Calificacion();
            cal.setUsuarioId(usuario.getId());
            cal.setPeliculaId(p.getId());
            cal.setPuntuacion(puntos);
            cal.setResena(resenaSql);
            if (!calificacionDAO.agregarCalificacion(cal)) {
                JOptionPane.showMessageDialog(this, "No se pudo guardar (¿duplicado?).", "MovieMatch", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (alCambiarDatos != null) {
            alCambiarDatos.run();
        }
        JOptionPane.showMessageDialog(this, "Calificación guardada.", "MovieMatch", JOptionPane.INFORMATION_MESSAGE);
        recargar();
    }

    private void eliminarSeleccionada() {
        if (ratingIdEdicion <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona una calificación en la lista o usa «Nueva» para crear.",
                    "MovieMatch",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Eliminar esta calificación?",
                "MovieMatch",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        if (!calificacionDAO.eliminarCalificacion(ratingIdEdicion, usuario.getId())) {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar.", "MovieMatch", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (alCambiarDatos != null) {
            alCambiarDatos.run();
        }
        recargar();
    }
}
