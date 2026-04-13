package moviematch.dao;

import moviematch.modelo.Calificacion;
import moviematch.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para calificaciones ({@code ratings}).
 */
public class CalificacionDAO {

    /** Fila lista para la UI: calificación con título de película. */
    public record CalificacionVista(
            int ratingId,
            int movieId,
            String tituloPelicula,
            int puntuacion,
            String resena,
            java.sql.Timestamp fecha) {
    }

    public boolean agregarCalificacion(Calificacion calificacion) {
        String sql = """
                INSERT INTO ratings (user_id, movie_id, rating, review)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, calificacion.getUsuarioId());
            ps.setInt(2, calificacion.getPeliculaId());
            ps.setInt(3, calificacion.getPuntuacion());
            ps.setString(4, calificacion.getResena());
            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        calificacion.setId(rs.getInt(1));
                    }
                }
            }
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("Error al agregar calificación: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarCalificacion(Calificacion calificacion) {
        String sql = "UPDATE ratings SET rating = ?, review = ? WHERE rating_id = ? AND user_id = ?";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, calificacion.getPuntuacion());
            ps.setString(2, calificacion.getResena());
            ps.setInt(3, calificacion.getId());
            ps.setInt(4, calificacion.getUsuarioId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar calificación: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarCalificacion(int ratingId, int usuarioId) {
        String sql = "DELETE FROM ratings WHERE rating_id = ? AND user_id = ?";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ratingId);
            ps.setInt(2, usuarioId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar calificación: " + e.getMessage());
            return false;
        }
    }

    public List<Calificacion> obtenerCalificacionesPorPelicula(int peliculaId) {
        List<Calificacion> lista = new ArrayList<>();
        String sql = """
                SELECT rating_id, user_id, movie_id, rating, review, rated_at
                FROM ratings WHERE movie_id = ? ORDER BY rated_at DESC
                """;
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, peliculaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar calificaciones por película: " + e.getMessage());
        }
        return lista;
    }

    public List<Calificacion> obtenerCalificacionesPorUsuario(int usuarioId) {
        List<Calificacion> lista = new ArrayList<>();
        String sql = """
                SELECT rating_id, user_id, movie_id, rating, review, rated_at
                FROM ratings WHERE user_id = ? ORDER BY rated_at DESC
                """;
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar calificaciones por usuario: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Calificaciones del usuario con nombre de película (para tablas/listas en Swing).
     */
    public List<CalificacionVista> obtenerCalificacionesVistaPorUsuario(int usuarioId) {
        List<CalificacionVista> lista = new ArrayList<>();
        String sql = """
                SELECT r.rating_id, r.movie_id, m.title, r.rating, r.review, r.rated_at
                FROM ratings r
                INNER JOIN movies m ON m.movie_id = r.movie_id
                WHERE r.user_id = ?
                ORDER BY r.rated_at DESC
                """;
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new CalificacionVista(
                            rs.getInt("rating_id"),
                            rs.getInt("movie_id"),
                            rs.getString("title"),
                            rs.getInt("rating"),
                            rs.getString("review"),
                            rs.getTimestamp("rated_at")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar calificaciones con título: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Si el usuario ya valoró esa película, devuelve el registro; si no, {@code null}.
     */
    public Calificacion obtenerPorUsuarioYPelicula(int usuarioId, int peliculaId) {
        String sql = """
                SELECT rating_id, user_id, movie_id, rating, review, rated_at
                FROM ratings WHERE user_id = ? AND movie_id = ?
                """;
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, peliculaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar calificación usuario/película: " + e.getMessage());
        }
        return null;
    }

    /**
     * Promedio de puntuaciones para una película; 0.0 si no hay calificaciones.
     */
    public double obtenerPromedioCalificaciones(int peliculaId) {
        String sql = "SELECT AVG(rating) AS promedio FROM ratings WHERE movie_id = ?";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, peliculaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double v = rs.getDouble("promedio");
                    return rs.wasNull() ? 0.0 : v;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular promedio de calificaciones: " + e.getMessage());
        }
        return 0.0;
    }

    private static Calificacion mapear(ResultSet rs) throws SQLException {
        return new Calificacion(
                rs.getInt("rating_id"),
                rs.getInt("user_id"),
                rs.getInt("movie_id"),
                rs.getInt("rating"),
                rs.getString("review"),
                rs.getTimestamp("rated_at"));
    }
}
