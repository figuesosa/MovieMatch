package moviematch.dao;

import moviematch.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Cache local de métricas externas de películas (TMDB) para reducir llamadas HTTP.
 */
public class MetricasExternasDAO {

    public record MetricasTmdb(
            int tmdbMovieId,
            double voteAverage,
            int voteCount,
            double popularity) {
    }

    public MetricasTmdb obtenerMetricasRecientes(int movieId, int maxHoras) {
        String sql = """
                SELECT tmdb_movie_id, tmdb_vote_average, tmdb_vote_count, tmdb_popularity
                FROM movie_external_metrics
                WHERE movie_id = ?
                  AND fetched_at >= DATE_SUB(NOW(), INTERVAL ? HOUR)
                """;
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.setInt(2, maxHoras);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new MetricasTmdb(
                            rs.getInt("tmdb_movie_id"),
                            rs.getDouble("tmdb_vote_average"),
                            rs.getInt("tmdb_vote_count"),
                            rs.getDouble("tmdb_popularity"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error leyendo métricas externas cacheadas: " + e.getMessage());
        }
        return null;
    }

    public boolean guardarOModificarMetricas(int movieId, MetricasTmdb m) {
        String sql = """
                INSERT INTO movie_external_metrics
                    (movie_id, tmdb_movie_id, tmdb_vote_average, tmdb_vote_count, tmdb_popularity, fetched_at)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                    tmdb_movie_id = VALUES(tmdb_movie_id),
                    tmdb_vote_average = VALUES(tmdb_vote_average),
                    tmdb_vote_count = VALUES(tmdb_vote_count),
                    tmdb_popularity = VALUES(tmdb_popularity),
                    fetched_at = CURRENT_TIMESTAMP
                """;
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.setInt(2, m.tmdbMovieId());
            ps.setDouble(3, m.voteAverage());
            ps.setInt(4, m.voteCount());
            ps.setDouble(5, m.popularity());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error guardando métricas externas: " + e.getMessage());
            return false;
        }
    }
}
