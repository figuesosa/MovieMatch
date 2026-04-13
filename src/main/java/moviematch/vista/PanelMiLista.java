package moviematch.vista;

import moviematch.dao.ListaPersonalDAO;
import moviematch.dao.PeliculaDAO;
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
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * Lista personal del usuario: favoritos y «ver más tarde» ({@code watchlist}).
 */
public class PanelMiLista extends JPanel {

    private final Usuario usuario;
    private final Runnable alCambiarDatos;
    private final ListaPersonalDAO listaDAO = new ListaPersonalDAO();
    private final PeliculaDAO peliculaDAO = new PeliculaDAO();

    private final DefaultListModel<String> modeloLista = new DefaultListModel<>();
    private final JList<String> lista = new JList<>(modeloLista);
    private final List<ListaPersonalDAO.EntradaLista> filas = new ArrayList<>();

    private final JComboBox<Pelicula> comboPelicula = new JComboBox<>();
    private final JComboBox<String> comboTipo = new JComboBox<>(new String[] { "Favorito", "Ver más tarde" });

    public PanelMiLista(Usuario usuario, Runnable alCambiarDatos) {
        super(new BorderLayout());
        this.usuario = usuario;
        this.alCambiarDatos = alCambiarDatos;

        JLabel ayuda = new JLabel(
                "<html>Añade películas como <b>favorito</b> o <b>ver más tarde</b>. "
                        + "Están excluidas de las recomendaciones mientras sigan en la lista.</html>");
        ayuda.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(lista);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 4, 3, 4);
        g.anchor = GridBagConstraints.LINE_START;
        g.gridx = 0;
        g.gridy = 0;
        form.add(new JLabel("Película:"), g);
        g.gridx = 1;
        form.add(comboPelicula, g);
        g.gridx = 0;
        g.gridy = 1;
        form.add(new JLabel("Tipo:"), g);
        g.gridx = 1;
        form.add(comboTipo, g);

        JButton anadir = new JButton("Añadir a mi lista");
        anadir.addActionListener(e -> anadir());
        JButton quitar = new JButton("Quitar selección");
        quitar.addActionListener(e -> quitarSeleccion());
        JButton refrescar = new JButton("Actualizar lista");
        refrescar.addActionListener(e -> recargar());

        JPanel botones = new JPanel();
        botones.add(anadir);
        botones.add(quitar);
        botones.add(refrescar);

        JPanel este = new JPanel(new BorderLayout());
        este.add(form, BorderLayout.NORTH);
        este.add(botones, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout(8, 0));
        centro.add(scroll, BorderLayout.CENTER);
        centro.add(este, BorderLayout.EAST);

        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(ayuda, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);

        rellenarCombo();
        recargar();
    }

    public void recargar() {
        modeloLista.clear();
        filas.clear();
        for (ListaPersonalDAO.EntradaLista e : listaDAO.listarEntradasUsuario(usuario.getId())) {
            filas.add(e);
            modeloLista.addElement(textoLinea(e));
        }
        rellenarCombo();
    }

    private static String textoLinea(ListaPersonalDAO.EntradaLista e) {
        String tipo = etiquetaEstado(e.status());
        return tipo + ": " + e.tituloPelicula();
    }

    private static String etiquetaEstado(String status) {
        if (ListaPersonalDAO.ESTADO_FAVORITO.equals(status)) {
            return "Favorito";
        }
        if (ListaPersonalDAO.ESTADO_VER_MAS_TARDE.equals(status)) {
            return "Ver más tarde";
        }
        return status;
    }

    private static String codigoEstadoUi(String etiqueta) {
        if ("Favorito".equals(etiqueta)) {
            return ListaPersonalDAO.ESTADO_FAVORITO;
        }
        return ListaPersonalDAO.ESTADO_VER_MAS_TARDE;
    }

    private void rellenarCombo() {
        comboPelicula.removeAllItems();
        for (Pelicula p : peliculaDAO.obtenerTodasLasPeliculas()) {
            comboPelicula.addItem(p);
        }
    }

    private void anadir() {
        Pelicula p = (Pelicula) comboPelicula.getSelectedItem();
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una película.", "MovieMatch", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String tipoUi = (String) comboTipo.getSelectedItem();
        String estado = codigoEstadoUi(tipoUi != null ? tipoUi : "Favorito");
        if (!listaDAO.agregarALista(usuario.getId(), p.getId(), estado)) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo añadir (¿ya está en la lista con ese tipo?).",
                    "MovieMatch",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (alCambiarDatos != null) {
            alCambiarDatos.run();
        }
        recargar();
    }

    private void quitarSeleccion() {
        int i = lista.getSelectedIndex();
        if (i < 0 || i >= filas.size()) {
            JOptionPane.showMessageDialog(this, "Selecciona una fila de la lista.", "MovieMatch", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ListaPersonalDAO.EntradaLista e = filas.get(i);
        if (!listaDAO.eliminarDeLista(usuario.getId(), e.movieId(), e.status())) {
            JOptionPane.showMessageDialog(this, "No se pudo quitar la entrada.", "MovieMatch", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (alCambiarDatos != null) {
            alCambiarDatos.run();
        }
        recargar();
    }
}
