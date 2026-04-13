package moviematch.vista;

import moviematch.modelo.Usuario;
import moviematch.util.NotificadorCambios;
import moviematch.util.RecursosUI;
import moviematch.util.SesionUsuario;
import moviematch.util.TemaAplicacion;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * Ventana única de la aplicación tras iniciar sesión: menú, cabecera y pestañas.
 */
public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal(Usuario usuario) {
        super("MovieMatch");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationByPlatform(true);

        configurarMenu();

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(TemaAplicacion.FONDO_SUPERFICIE);
        cabecera.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JPanel marcaFila = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        marcaFila.setOpaque(false);
        ImageIcon icono = RecursosUI.iconoCabecera(36);
        if (icono != null) {
            marcaFila.add(new JLabel(icono));
        }
        JLabel marca = new JLabel("MovieMatch", SwingConstants.LEFT);
        marca.setFont(marca.getFont().deriveFont(Font.BOLD, 18f));
        marca.setForeground(new Color(0xEE, 0xF1, 0xF5));
        marcaFila.add(marca);
        JLabel sesion = new JLabel("Sesión: " + usuario.getNombreUsuario(), SwingConstants.RIGHT);
        sesion.setForeground(TemaAplicacion.TEXTO_SUAVE);
        cabecera.add(marcaFila, BorderLayout.WEST);
        cabecera.add(sesion, BorderLayout.EAST);

        NotificadorCambios cambios = new NotificadorCambios();
        PanelRecomendaciones panelRecomendaciones = new PanelRecomendaciones(usuario);
        cambios.suscribir(panelRecomendaciones::cargarRecomendaciones);

        PanelPreferencias panelPreferencias = new PanelPreferencias(usuario, cambios::notificar);
        PanelCatalogoPeliculas panelCatalogo = new PanelCatalogoPeliculas(cambios::notificar);
        PanelCalificaciones panelCalificaciones = new PanelCalificaciones(usuario, cambios::notificar);
        PanelMiLista panelMiLista = new PanelMiLista(usuario, cambios::notificar);

        cambios.suscribir(panelCatalogo::recargar);
        cambios.suscribir(panelCalificaciones::recargar);
        cambios.suscribir(panelMiLista::recargar);

        JTabbedPane pestañas = new JTabbedPane();
        pestañas.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        pestañas.addTab("Recomendaciones", panelRecomendaciones);
        pestañas.addTab("Géneros preferidos", panelPreferencias);
        pestañas.addTab("Catálogo de películas", panelCatalogo);
        pestañas.addTab("Mis calificaciones", panelCalificaciones);
        pestañas.addTab("Mi lista", panelMiLista);

        add(cabecera, BorderLayout.NORTH);
        add(pestañas, BorderLayout.CENTER);

        setSize(780, 560);
        setMinimumSize(new Dimension(640, 420));

        RecursosUI.aplicarIconoVentana(this);
    }

    private void configurarMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu archivo = new JMenu("Archivo");
        JMenuItem cerrarSesion = new JMenuItem("Cerrar sesión…");
        cerrarSesion.addActionListener(e -> confirmarCerrarSesion());
        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(e -> System.exit(0));
        archivo.add(cerrarSesion);
        archivo.addSeparator();
        archivo.add(salir);

        JMenu ayuda = new JMenu("Ayuda");
        JMenuItem acerca = new JMenuItem("Acerca de MovieMatch");
        acerca.addActionListener(e -> mostrarAcercaDe());
        ayuda.add(acerca);

        barra.add(archivo);
        barra.add(ayuda);
        setJMenuBar(barra);
    }

    private void confirmarCerrarSesion() {
        int r = JOptionPane.showConfirmDialog(this,
                "¿Cerrar sesión y volver al inicio de sesión?",
                "MovieMatch",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }
        SesionUsuario.cerrarSesion();
        dispose();
        VentanaLogin.mostrarLoginTrasCerrarSesion();
    }

    private void mostrarAcercaDe() {
        JOptionPane.showMessageDialog(this,
                "MovieMatch — recomendaciones según géneros preferidos, popularidad y valoraciones.\n"
                        + "Incluye catálogo, calificaciones y lista personal (favoritos / ver más tarde).\n"
                        + "Proyecto académico (Java Swing + MySQL).",
                "Acerca de MovieMatch",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
