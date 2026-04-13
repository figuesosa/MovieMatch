package moviematch.vista;

import moviematch.dao.UsuarioDAO;
import moviematch.modelo.Usuario;
import moviematch.util.RecursosUI;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

/**
 * Registro de un nuevo usuario (tabla {@code users}).
 */
public class VentanaRegistro extends JDialog {

    private final JTextField campoUsuario = new JTextField(18);
    private final JTextField campoEmail = new JTextField(18);
    private final JPasswordField campoClave = new JPasswordField(18);
    private final JPasswordField campoClave2 = new JPasswordField(18);
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private boolean registroExitoso;
    private String nombreUsuarioCreado;

    public VentanaRegistro(Window propietario) {
        super(propietario, "MovieMatch — Crear cuenta", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);
        RecursosUI.aplicarIconoVentana(this);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.LINE_END;

        g.gridx = 0;
        g.gridy = 0;
        formulario.add(new JLabel("Usuario:"), g);
        g.gridy = 1;
        formulario.add(new JLabel("Email:"), g);
        g.gridy = 2;
        formulario.add(new JLabel("Contraseña:"), g);
        g.gridy = 3;
        formulario.add(new JLabel("Repetir contraseña:"), g);

        g.anchor = GridBagConstraints.LINE_START;
        g.gridx = 1;
        g.gridy = 0;
        formulario.add(campoUsuario, g);
        g.gridy = 1;
        formulario.add(campoEmail, g);
        g.gridy = 2;
        formulario.add(campoClave, g);
        g.gridy = 3;
        formulario.add(campoClave2, g);

        JButton crear = new JButton("Registrarse");
        crear.addActionListener(e -> intentarRegistro());
        JButton cancelar = new JButton("Cancelar");
        cancelar.addActionListener(e -> dispose());

        JPanel sur = new JPanel();
        sur.add(crear);
        sur.add(cancelar);

        add(formulario, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
        pack();
        setMinimumSize(getSize());
    }

    public boolean isRegistroExitoso() {
        return registroExitoso;
    }

    /** Nombre del usuario recién registrado; {@code null} si no hubo registro. */
    public String getNombreUsuarioCreado() {
        return nombreUsuarioCreado;
    }

    private void intentarRegistro() {
        String nombre = campoUsuario.getText().trim();
        String email = campoEmail.getText().trim();
        char[] c1 = campoClave.getPassword();
        char[] c2 = campoClave2.getPassword();
        String clave = new String(c1);
        String claveRep = new String(c2);
        java.util.Arrays.fill(c1, '\0');
        java.util.Arrays.fill(c2, '\0');

        if (nombre.isEmpty() || email.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Completa usuario, email y contraseña.",
                    "MovieMatch",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (nombre.length() > 50) {
            JOptionPane.showMessageDialog(this,
                    "El usuario no puede superar 50 caracteres.",
                    "MovieMatch",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (email.length() > 100) {
            JOptionPane.showMessageDialog(this,
                    "El email no puede superar 100 caracteres.",
                    "MovieMatch",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!email.contains("@")) {
            JOptionPane.showMessageDialog(this,
                    "Introduce un email válido.",
                    "MovieMatch",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!clave.equals(claveRep)) {
            JOptionPane.showMessageDialog(this,
                    "Las contraseñas no coinciden.",
                    "MovieMatch",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (usuarioDAO.existeNombreUsuario(nombre)) {
            JOptionPane.showMessageDialog(this,
                    "Ese nombre de usuario ya está en uso.",
                    "MovieMatch",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (usuarioDAO.existeEmail(email)) {
            JOptionPane.showMessageDialog(this,
                    "Ese email ya está registrado.",
                    "MovieMatch",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombreUsuario(nombre);
        nuevo.setEmail(email);
        nuevo.setContrasena(clave);

        if (!usuarioDAO.crearUsuario(nuevo)) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo crear la cuenta (revisa la conexión a MySQL o datos duplicados).",
                    "MovieMatch",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        registroExitoso = true;
        nombreUsuarioCreado = nombre;
        JOptionPane.showMessageDialog(this,
                "Cuenta creada. Ya puedes iniciar sesión.",
                "MovieMatch",
                JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
