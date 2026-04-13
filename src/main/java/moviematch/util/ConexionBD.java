package moviematch.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utilidad para obtener conexiones JDBC a la base de datos MySQL {@code moviematch}.
 * Aplicación MovieMatch — motor de recomendación de películas.
 */
public final class ConexionBD {

    /** URL JDBC con zona horaria UTC y SSL desactivado (entorno de desarrollo). */
    private static final String URL =
            "jdbc:mysql://localhost:3306/moviematch?useSSL=false&serverTimezone=UTC";

    /** Usuario de la base de datos. */
    private static final String USUARIO = "root";

    /** Contraseña del usuario de la base de datos. */
    private static final String CONTRASENA = "root#$#212";

    private ConexionBD() {
        // Solo métodos estáticos
    }

    /**
     * Obtiene una conexión nueva a la base de datos {@code moviematch}.
     *
     * @return conexión JDBC abierta; el llamador debe cerrarla (try-with-resources)
     * @throws SQLException si falla el registro del driver o la conexión al servidor
     */
    public static Connection getConexion() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "No se pudo cargar el driver com.mysql.cj.jdbc.Driver (MySQL Connector/J).", e);
        }
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
}
