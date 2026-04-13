package moviematch.vista;

import moviematch.dao.GeneroDAO;
import moviematch.dao.PeliculaDAO;
import moviematch.modelo.Genero;
import moviematch.modelo.Pelicula;
import moviematch.modelo.PeliculaExterna;
import moviematch.servicio.TmdbService;
import moviematch.util.CachePosters;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Alta, busqueda, edicion y baja de peliculas, con importacion opcional desde TMDB.
 */
public class PanelCatalogoPeliculas extends JPanel {

    private final Runnable alCambiarDatos;
    private final PeliculaDAO peliculaDAO = new PeliculaDAO();
    private final GeneroDAO generoDAO = new GeneroDAO();
    private final TmdbService tmdbService = new TmdbService();

    private final DefaultListModel<Pelicula> modeloPeliculas = new DefaultListModel<>();
    private final JList<Pelicula> listaPeliculas = new JList<>(modeloPeliculas);
    private final List<Genero> todosGeneros = new ArrayList<>();
    private final DefaultListModel<Genero> modeloGenerosUi = new DefaultListModel<>();
    private final JList<Genero> listaGeneros = new JList<>(modeloGenerosUi);

    private final JTextField campoBuscar = new JTextField(18);
    private final JTextField campoTitulo = new JTextField(24);
    private final JTextArea campoDescripcion = new JTextArea(4, 24);
    private final JTextField campoAnio = new JTextField(6);
    private final JTextField campoDirector = new JTextField(20);
    private final JTextField campoPopularidad = new JTextField(8);
    private final JComboBox<String> comboIdiomaTmdb = new JComboBox<>(new String[] {"English (en-US)", "Español (es-ES)"});
    private final JLabel etiquetaId = new JLabel("-", SwingConstants.LEFT);
    private final JLabel etiquetaPoster = new JLabel("Sin poster", SwingConstants.CENTER);

    private int idEdicion;
    private String posterUrlEdicion;

    public PanelCatalogoPeliculas(Runnable alCambiarDatos) {
        super(new BorderLayout());
        this.alCambiarDatos = alCambiarDatos;

        JLabel aviso = new JLabel(
                "<html><b>Catalogo</b> — Crea/edita peliculas locales o importalas desde internet (TMDB).</html>");
        aviso.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        listaPeliculas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaPeliculas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionLista();
            }
        });
        JScrollPane scrollPelis = new JScrollPane(listaPeliculas);
        scrollPelis.setPreferredSize(new Dimension(220, 290));

        cargarGenerosDisponibles();
        listaGeneros.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollGen = new JScrollPane(listaGeneros);
        scrollGen.setPreferredSize(new Dimension(220, 125));

        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.anchor = GridBagConstraints.LINE_START;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formulario.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        formulario.add(etiquetaId, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formulario.add(new JLabel("Titulo:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        formulario.add(campoTitulo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formulario.add(new JLabel("Descripcion:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        formulario.add(new JScrollPane(campoDescripcion), gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        formulario.add(new JLabel("Anio:"), gbc);
        gbc.gridx = 1;
        formulario.add(campoAnio, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formulario.add(new JLabel("Director:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formulario.add(campoDirector, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        formulario.add(new JLabel("Popularidad:"), gbc);
        gbc.gridx = 1;
        formulario.add(campoPopularidad, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        formulario.add(new JLabel("Generos (Ctrl+clic para varios):"), gbc);
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.6;
        formulario.add(scrollGen, gbc);

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.add(formulario, BorderLayout.NORTH);
        etiquetaPoster.setPreferredSize(new Dimension(170, 240));
        etiquetaPoster.setBorder(BorderFactory.createTitledBorder("Poster"));
        derecha.add(etiquetaPoster, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPelis, derecha);
        split.setResizeWeight(0.32);

        JPanel norteBusqueda = new JPanel();
        norteBusqueda.add(new JLabel("Buscar local:"));
        norteBusqueda.add(campoBuscar);
        JButton buscar = new JButton("Buscar");
        buscar.addActionListener(e -> ejecutarBusquedaLocal());
        JButton listar = new JButton("Listar todas");
        listar.addActionListener(e -> recargarListaCompleta());
        JButton buscarOnline = new JButton("Buscar online (TMDB)");
        buscarOnline.addActionListener(e -> buscarOnlineTmdb());
        norteBusqueda.add(new JLabel("Idioma API:"));
        norteBusqueda.add(comboIdiomaTmdb);
        norteBusqueda.add(buscar);
        norteBusqueda.add(listar);
        norteBusqueda.add(buscarOnline);

        JButton nueva = new JButton("Nueva pelicula");
        nueva.addActionListener(e -> prepararNueva());
        JButton guardar = new JButton("Guardar");
        guardar.addActionListener(e -> guardar());
        JButton eliminar = new JButton("Eliminar");
        eliminar.addActionListener(e -> eliminarSeleccionada());
        JButton refrescar = new JButton("Actualizar lista");
        refrescar.addActionListener(e -> recargarListaCompleta());

        JPanel sur = new JPanel();
        sur.add(nueva);
        sur.add(guardar);
        sur.add(eliminar);
        sur.add(refrescar);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(norteBusqueda, BorderLayout.NORTH);
        centro.add(split, BorderLayout.CENTER);

        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(aviso, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);

        recargarListaCompleta();
    }

    public void recargar() {
        Pelicula sel = listaPeliculas.getSelectedValue();
        int idSel = sel != null ? sel.getId() : 0;
        recargarListaCompleta();
        if (idSel > 0) {
            seleccionarPeliculaEnLista(idSel);
        }
    }

    private void cargarGenerosDisponibles() {
        todosGeneros.clear();
        modeloGenerosUi.clear();
        for (Genero g : generoDAO.obtenerTodosLosGeneros()) {
            todosGeneros.add(g);
            modeloGenerosUi.addElement(g);
        }
    }

    private void ejecutarBusquedaLocal() {
        String q = campoBuscar.getText().trim();
        modeloPeliculas.clear();
        List<Pelicula> r = q.isEmpty() ? peliculaDAO.obtenerTodasLasPeliculas() : peliculaDAO.buscarPeliculas(q);
        for (Pelicula p : r) {
            modeloPeliculas.addElement(p);
        }
        limpiarFormulario();
        idEdicion = 0;
        etiquetaId.setText("—");
    }

    private void buscarOnlineTmdb() {
        if (!tmdbService.estaConfigurado()) {
            JOptionPane.showMessageDialog(this,
                    "TMDB no esta configurado.\n"
                            + "Define la variable de entorno TMDB_BEARER_TOKEN y reinicia la app.",
                    "MovieMatch",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String q = campoBuscar.getText().trim();
        if (q.isEmpty()) {
            q = JOptionPane.showInputDialog(this, "Texto a buscar en TMDB:", "MovieMatch", JOptionPane.QUESTION_MESSAGE);
            if (q == null || q.trim().isEmpty()) {
                return;
            }
        }
        List<PeliculaExterna> encontrados = tmdbService.buscarPeliculas(q.trim(), idiomaSeleccionadoTmdb());
        if (encontrados.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No se encontraron resultados online para \"" + q + "\".",
                    "MovieMatch",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        DefaultListModel<PeliculaExterna> modelo = new DefaultListModel<>();
        for (PeliculaExterna pe : encontrados) {
            modelo.addElement(pe);
        }
        JList<PeliculaExterna> listaOnline = new JList<>(modelo);
        listaOnline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaOnline.setVisibleRowCount(12);
        if (!modelo.isEmpty()) {
            listaOnline.setSelectedIndex(0);
        }
        JScrollPane scroll = new JScrollPane(listaOnline);
        scroll.setPreferredSize(new Dimension(520, 260));

        int r = JOptionPane.showConfirmDialog(this,
                scroll,
                "Resultados TMDB - selecciona y pulsa OK para importar",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }
        PeliculaExterna seleccion = listaOnline.getSelectedValue();
        if (seleccion == null) {
            return;
        }
        importarPeliculaExterna(seleccion);
    }

    private void importarPeliculaExterna(PeliculaExterna ext) {
        Pelicula p = new Pelicula();
        p.setTitulo(ext.getTitulo());
        p.setDescripcion(ext.getDescripcion());
        p.setAnioLanzamiento(ext.getAnioLanzamiento());
        p.setDirector("TMDB");
        p.setPopularidad(ext.getPopularidad());
        p.setPosterUrl(ext.getPosterUrl());

        if (!peliculaDAO.agregarPelicula(p)) {
            JOptionPane.showMessageDialog(this, "No se pudo importar la pelicula.", "MovieMatch", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Integer> genreIds = new ArrayList<>();
        for (String nombreGenero : ext.getGeneros()) {
            int id = resolverOCrearGenero(nombreGenero);
            if (id > 0) {
                genreIds.add(id);
            }
        }
        if (!genreIds.isEmpty() && !peliculaDAO.reemplazarGenerosPelicula(p.getId(), genreIds)) {
            JOptionPane.showMessageDialog(this,
                    "Pelicula importada, pero no se pudieron asignar todos los generos.",
                    "MovieMatch",
                    JOptionPane.WARNING_MESSAGE);
        }

        cargarGenerosDisponibles();
        if (alCambiarDatos != null) {
            alCambiarDatos.run();
        }
        recargarListaCompleta();
        seleccionarPeliculaEnLista(p.getId());
        JOptionPane.showMessageDialog(this,
                "Importada desde TMDB: " + ext.getTitulo(),
                "MovieMatch",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private int resolverOCrearGenero(String nombreTmdb) {
        String objetivo = normalizar(nombreTmdb);
        for (Genero g : generoDAO.obtenerTodosLosGeneros()) {
            if (normalizar(g.getNombre()).equals(objetivo)) {
                return g.getId();
            }
        }
        int nuevoId = generoDAO.crearGenero(nombreTmdb.trim());
        if (nuevoId > 0) {
            return nuevoId;
        }
        Genero existente = generoDAO.obtenerGeneroPorNombreExacto(nombreTmdb.trim());
        return existente != null ? existente.getId() : -1;
    }

    private static String normalizar(String txt) {
        if (txt == null) {
            return "";
        }
        String base = Normalizer.normalize(txt, Normalizer.Form.NFD);
        base = base.replaceAll("\\p{M}+", "");
        return base.toLowerCase(Locale.ROOT).trim();
    }

    private String idiomaSeleccionadoTmdb() {
        Object sel = comboIdiomaTmdb.getSelectedItem();
        String txt = sel == null ? "" : sel.toString();
        return txt.startsWith("Español") ? "es-ES" : "en-US";
    }

    private void recargarListaCompleta() {
        campoBuscar.setText("");
        modeloPeliculas.clear();
        for (Pelicula p : peliculaDAO.obtenerTodasLasPeliculas()) {
            modeloPeliculas.addElement(p);
        }
        listaPeliculas.clearSelection();
        limpiarFormulario();
        idEdicion = 0;
        etiquetaId.setText("—");
    }

    private void seleccionarPeliculaEnLista(int movieId) {
        for (int i = 0; i < modeloPeliculas.size(); i++) {
            if (modeloPeliculas.getElementAt(i).getId() == movieId) {
                listaPeliculas.setSelectedIndex(i);
                break;
            }
        }
    }

    private void cargarSeleccionLista() {
        Pelicula p = listaPeliculas.getSelectedValue();
        if (p == null) {
            return;
        }
        idEdicion = p.getId();
        etiquetaId.setText(String.valueOf(p.getId()));
        campoTitulo.setText(p.getTitulo());
        campoDescripcion.setText(p.getDescripcion() != null ? p.getDescripcion() : "");
        campoAnio.setText(p.getAnioLanzamiento() > 0 ? String.valueOf(p.getAnioLanzamiento()) : "");
        campoDirector.setText(p.getDirector() != null ? p.getDirector() : "");
        campoPopularidad.setText(String.valueOf(p.getPopularidad()));
        posterUrlEdicion = p.getPosterUrl();
        actualizarPreviewPoster(p);

        List<Integer> gids = peliculaDAO.obtenerIdsGenerosDePelicula(p.getId());
        listaGeneros.clearSelection();
        List<Integer> idxList = new ArrayList<>();
        for (int i = 0; i < todosGeneros.size(); i++) {
            if (gids.contains(todosGeneros.get(i).getId())) {
                idxList.add(i);
            }
        }
        int[] sel = idxList.stream().mapToInt(Integer::intValue).toArray();
        listaGeneros.setSelectedIndices(sel);
    }

    private void prepararNueva() {
        listaPeliculas.clearSelection();
        idEdicion = 0;
        posterUrlEdicion = null;
        etiquetaId.setText("(nueva)");
        limpiarFormulario();
    }

    private void limpiarFormulario() {
        campoTitulo.setText("");
        campoDescripcion.setText("");
        campoAnio.setText("");
        campoDirector.setText("");
        campoPopularidad.setText("5.0");
        listaGeneros.clearSelection();
        etiquetaPoster.setIcon(null);
        etiquetaPoster.setText("Sin poster");
    }

    private List<Integer> generosSeleccionados() {
        List<Integer> out = new ArrayList<>();
        for (Genero g : listaGeneros.getSelectedValuesList()) {
            out.add(g.getId());
        }
        return out;
    }

    private void guardar() {
        String titulo = campoTitulo.getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El titulo es obligatorio.", "MovieMatch", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double pop;
        try {
            pop = Double.parseDouble(campoPopularidad.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Popularidad debe ser un numero.", "MovieMatch", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int anio = 0;
        String anioTxt = campoAnio.getText().trim();
        if (!anioTxt.isEmpty()) {
            try {
                anio = Integer.parseInt(anioTxt);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Anio no valido.", "MovieMatch", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        Pelicula p = new Pelicula();
        p.setTitulo(titulo);
        p.setDescripcion(campoDescripcion.getText().trim());
        p.setAnioLanzamiento(anio);
        p.setDirector(campoDirector.getText().trim());
        p.setPopularidad(pop);
        p.setPosterUrl(idEdicion > 0 ? posterUrlEdicion : null);
        List<Integer> gids = generosSeleccionados();

        if (idEdicion <= 0) {
            if (!peliculaDAO.agregarPelicula(p)) {
                JOptionPane.showMessageDialog(this, "No se pudo crear la pelicula.", "MovieMatch", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!gids.isEmpty() && !peliculaDAO.reemplazarGenerosPelicula(p.getId(), gids)) {
                JOptionPane.showMessageDialog(this, "Pelicula creada pero fallo asignar generos.", "MovieMatch", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            p.setId(idEdicion);
            if (!peliculaDAO.actualizarPelicula(p)) {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar la pelicula.", "MovieMatch", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!peliculaDAO.reemplazarGenerosPelicula(idEdicion, gids)) {
                JOptionPane.showMessageDialog(this, "Pelicula guardada pero fallo actualizar generos.", "MovieMatch", JOptionPane.WARNING_MESSAGE);
            }
        }

        if (alCambiarDatos != null) {
            alCambiarDatos.run();
        }
        JOptionPane.showMessageDialog(this, "Pelicula guardada.", "MovieMatch", JOptionPane.INFORMATION_MESSAGE);
        int idSel = p.getId();
        recargar();
        seleccionarPeliculaEnLista(idSel);
    }

    private void eliminarSeleccionada() {
        if (idEdicion <= 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una pelicula de la lista.", "MovieMatch", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Eliminar esta pelicula de la base? Se borraran calificaciones y entradas de lista asociadas (CASCADE).",
                "MovieMatch",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        if (!peliculaDAO.eliminarPelicula(idEdicion)) {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar.", "MovieMatch", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (alCambiarDatos != null) {
            alCambiarDatos.run();
        }
        recargarListaCompleta();
    }

    private void actualizarPreviewPoster(Pelicula pelicula) {
        etiquetaPoster.setIcon(null);
        etiquetaPoster.setText("Cargando...");
        new SwingWorker<javax.swing.ImageIcon, Void>() {
            @Override
            protected javax.swing.ImageIcon doInBackground() {
                return CachePosters.get().obtenerPosterEscalado(pelicula, 160, 230);
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
