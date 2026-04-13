package moviematch.util;

import moviematch.modelo.Usuario;

import java.util.Arrays;

/**
 * Usuario autenticado en memoria durante la ejecución de la aplicación.
 * Se limpia al cerrar sesión; las credenciales recordadas también se borran al cerrar sesión.
 */
public final class SesionUsuario {

    private static Usuario usuarioActual;
    /** Solo en memoria; opcional desde el login hasta cerrar sesión o salir de la app. */
    private static String usuarioRecordado;
    private static char[] contrasenaRecordada;

    private SesionUsuario() {
    }

    public static void iniciarSesion(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static boolean haySesionActiva() {
        return usuarioActual != null;
    }

    /**
     * Deja de considerar al usuario autenticado. Las credenciales guardadas con
     * {@link #setRecordarCredenciales} no se borran aquí (siguen en memoria hasta salir de la app).
     */
    public static void cerrarSesion() {
        usuarioActual = null;
    }

    /** Borra usuario en sesión y credenciales recordadas (p. ej. al cerrar la aplicación). */
    public static void borrarTodaLaInformacionEnMemoria() {
        usuarioActual = null;
        usuarioRecordado = null;
        if (contrasenaRecordada != null) {
            Arrays.fill(contrasenaRecordada, '\0');
            contrasenaRecordada = null;
        }
    }

    public static void setRecordarCredenciales(boolean recordar, String usuario, char[] contrasena) {
        if (!recordar) {
            usuarioRecordado = null;
            if (contrasenaRecordada != null) {
                Arrays.fill(contrasenaRecordada, '\0');
                contrasenaRecordada = null;
            }
            return;
        }
        usuarioRecordado = usuario != null ? usuario.trim() : null;
        if (contrasenaRecordada != null) {
            Arrays.fill(contrasenaRecordada, '\0');
        }
        contrasenaRecordada = contrasena != null ? Arrays.copyOf(contrasena, contrasena.length) : null;
    }

    public static String getUsuarioRecordado() {
        return usuarioRecordado;
    }

    /**
     * Copia para rellenar el campo de contraseña; no modifica el almacenamiento interno.
     */
    public static char[] copiaContrasenaRecordada() {
        return contrasenaRecordada == null ? null : Arrays.copyOf(contrasenaRecordada, contrasenaRecordada.length);
    }
}
