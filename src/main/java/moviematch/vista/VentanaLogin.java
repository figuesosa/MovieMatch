package moviematch.vista;

import moviematch.dao.UsuarioDAO;
import moviematch.modelo.Usuario;
import moviematch.util.RecursosUI;
import moviematch.util.SesionUsuario;
import moviematch.util.TemaAplicacion;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;

/**
 * Ventana de inicio de sesión simple (Swing) para MovieMatch.
 */
public class VentanaLogin extends JFrame {

    private final JTextField campoUsuario = new JTextField(20);
    private final JPasswordField campoContrasena = new JPasswordField(20);
    private final JCheckBox recordarCredenciales = new JCheckBox(
            "Recordar usuario y contraseña (solo en memoria, hasta cerrar la aplicación)");
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public VentanaLogin() {
        super("MovieMatch — Iniciar sesión");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationByPlatform(true);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(TemaAplicacion.FONDO_SUPERFICIE);
        cabecera.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));
        JPanel filaMarca = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        filaMarca.setOpaque(false);
        ImageIcon icono = RecursosUI.iconoCabecera(40);
        if (icono != null) {
            filaMarca.add(new JLabel(icono));
        }
        JLabel tituloApp = new JLabel("MovieMatch");
        tituloApp.setFont(tituloApp.getFont().deriveFont(Font.BOLD, 22f));
        tituloApp.setForeground(new Color(0xEE, 0xF1, 0xF5));
        filaMarca.add(tituloApp);
        cabecera.add(filaMarca, BorderLayout.CENTER);
        JLabel subtitulo = new JLabel("Iniciar sesión", SwingConstants.CENTER);
        subtitulo.setForeground(TemaAplicacion.TEXTO_SUAVE);
        cabecera.add(subtitulo, BorderLayout.SOUTH);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.LINE_END;

        g.gridx = 0;
        g.gridy = 0;
        formulario.add(new JLabel("Usuario:"), g);
        g.gridy = 1;
        formulario.add(new JLabel("Contraseña:"), g);

        g.anchor = GridBagConstraints.LINE_START;
        g.gridx = 1;
        g.gridy = 0;
        formulario.add(campoUsuario, g);
        g.gridy = 1;
        formulario.add(campoContrasena, g);

        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 2;
        g.anchor = GridBagConstraints.LINE_START;
        formulario.add(recordarCredenciales, g);

        JButton entrar = new JButton("Entrar");
        entrar.addActionListener(e -> intentarLogin());
        JButton registrar = new JButton("Crear cuenta");
        registrar.addActionListener(e -> abrirRegistro());

        JPanel sur = new JPanel();
        sur.add(entrar);
        sur.add(registrar);

        add(cabecera, BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
        pack();
        setMinimumSize(getSize());

        RecursosUI.aplicarIconoVentana(this);
        aplicarCredencialesSiHay();
    }

    private void aplicarCredencialesSiHay() {
        String u = SesionUsuario.getUsuarioRecordado();
        if (u != null) {
            campoUsuario.setText(u);
        }
        char[] copia = SesionUsuario.copiaContrasenaRecordada();
        if (copia != null) {
            campoContrasena.setText(new String(copia));
            Arrays.fill(copia, '\0');
        }
    }

    private void abrirRegistro() {
        VentanaRegistro dlg = new VentanaRegistro(this);
        dlg.setVisible(true);
        if (dlg.isRegistroExitoso()) {
            String creado = dlg.getNombreUsuarioCreado();
            if (creado != null) {
                campoUsuario.setText(creado);
            }
            campoContrasena.setText("");
        }
    }

    private void intentarLogin() {
        String nombre = campoUsuario.getText().trim();
        char[] passChars = campoContrasena.getPassword();
        String clave = new String(passChars);
        Arrays.fill(passChars, '\0');

        if (nombre.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Introduce usuario y contraseña.",
                    "MovieMatch",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!usuarioDAO.validarLogin(nombre, clave)) {
            JOptionPane.showMessageDialog(this,
                    "Usuario o contraseña incorrectos.",
                    "MovieMatch",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        Usuario u = usuarioDAO.obtenerUsuarioPorNombre(nombre);
        if (u == null) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo cargar el usuario.",
                    "MovieMatch",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (recordarCredenciales.isSelected()) {
            SesionUsuario.setRecordarCredenciales(true, nombre, clave.toCharArray());
        } else {
            SesionUsuario.setRecordarCredenciales(false, null, null);
        }
        SesionUsuario.iniciarSesion(u);

        dispose();
        SwingUtilities.invokeLater(() -> new VentanaPrincipal(u).setVisible(true));
    }

    public static void mostrarLoginTrasCerrarSesion() {
        SwingUtilities.invokeLater(() -> new VentanaLogin().setVisible(true));
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(SesionUsuario::borrarTodaLaInformacionEnMemoria));
        TemaAplicacion.instalar();

        SwingUtilities.invokeLater(() -> new VentanaLogin().setVisible(true));
    }
}
