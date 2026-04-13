package moviematch.vista;

import moviematch.dao.GeneroDAO;
import moviematch.dao.PreferenciaDAO;
import moviematch.modelo.Genero;
import moviematch.modelo.Usuario;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Edición de géneros preferidos embebida en la ventana principal (pestaña).
 */
public class PanelPreferencias extends JPanel {

    private final Usuario usuario;
    private final PreferenciaDAO preferenciaDAO = new PreferenciaDAO();
    private final GeneroDAO generoDAO = new GeneroDAO();
    private final Map<Integer, JCheckBox> casillasPorGeneroId = new HashMap<>();
    private final Runnable alGuardarExitoso;

    public PanelPreferencias(Usuario usuario, Runnable alGuardarExitoso) {
        super(new BorderLayout());
        this.usuario = usuario;
        this.alGuardarExitoso = alGuardarExitoso;

        JLabel ayuda = new JLabel(
                "<html>Marca los géneros que te interesan. Las recomendaciones usan estas preferencias. "
                        + "Pulsa <b>Guardar</b> y luego revisa la pestaña <b>Recomendaciones</b>.</html>");
        ayuda.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel listaPanel = new JPanel();
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS));
        listaPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        List<Genero> generos = generoDAO.obtenerTodosLosGeneros();
        Set<Integer> elegidos = new HashSet<>(preferenciaDAO.obtenerPreferenciasPorUsuario(usuario.getId()));

        if (generos.isEmpty()) {
            listaPanel.add(new JLabel("No hay géneros en la base de datos."));
        } else {
            for (Genero g : generos) {
                JCheckBox cb = new JCheckBox(g.getNombre());
                cb.setAlignmentX(JCheckBox.LEFT_ALIGNMENT);
                cb.setSelected(elegidos.contains(g.getId()));
                casillasPorGeneroId.put(g.getId(), cb);
                listaPanel.add(cb);
                listaPanel.add(Box.createRigidArea(new Dimension(0, 4)));
            }
        }

        JScrollPane scroll = new JScrollPane(listaPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JButton guardar = new JButton("Guardar preferencias");
        guardar.addActionListener(e -> guardarCambios());
        JPanel sur = new JPanel();
        sur.add(guardar);

        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(ayuda, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
    }

    private void guardarCambios() {
        Set<Integer> nuevos = new HashSet<>();
        for (Map.Entry<Integer, JCheckBox> e : casillasPorGeneroId.entrySet()) {
            if (e.getValue().isSelected()) {
                nuevos.add(e.getKey());
            }
        }

        Set<Integer> anteriores = new HashSet<>(
                preferenciaDAO.obtenerPreferenciasPorUsuario(usuario.getId()));

        for (Integer id : anteriores) {
            if (!nuevos.contains(id) && !preferenciaDAO.eliminarPreferencia(usuario.getId(), id)) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo quitar una preferencia (revisa la conexión).",
                        "MovieMatch",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        for (Integer id : nuevos) {
            if (!anteriores.contains(id) && !preferenciaDAO.agregarPreferencia(usuario.getId(), id)) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo añadir una preferencia (¿duplicado o error de conexión?).",
                        "MovieMatch",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (alGuardarExitoso != null) {
            alGuardarExitoso.run();
        }
        JOptionPane.showMessageDialog(this,
                "Preferencias guardadas. Revisa la pestaña Recomendaciones.",
                "MovieMatch",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
