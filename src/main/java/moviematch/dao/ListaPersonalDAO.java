package moviematch.dao;

import moviematch.modelo.Pelicula;
import moviematch.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Lista personal del usuario: favoritos y “ver más tarde” (tabla {@code watchlist}).
 */
public class ListaPersonalDAO {

    public static final String ESTADO_FAVORITO = "FAVORITE";
    public static final String ESTADO_VER_MAS_TARDE = "WATCH_LATER";

    private static final String FAVORITE = ESTADO_FAVORITO;
    private static final String WATCH_LATER = ESTADO_VER_MAS_TARDE;

    /** Entrada de {@code watchlist} con título para la interfaz. */
    public record EntradaLista(
            int watchlistId,
            int movieId,
            String tituloPelicula,
            String status,
            java.sql.Timestamp fechaAgregada) {
    }

    /**
     * @param estado {@code FAVORITE} o {@code WATCH_LATER}
     */
    public boolean agregarALista(int usuarioId, int peliculaId, String estado) {
        if (!esEstadoValido(estado)) {
            System.err.println("Estado de lista no válido: " + estado);
            return false;
        }
        String sql = "INSERT INTO watchlist (user_id, movie_id, status) VALUES (?, ?, ?)";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, peliculaId);
            ps.setString(3, estado);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al agregar a la lista: " + e.getMessage());
            return false;
        }
    }

    /**
     * @param estado {@code FAVORITE} o {@code WATCH_LATER}
     */
    public boolean eliminarDeLista(int usuarioId, int peliculaId, String estado) {
        if (!esEstadoValido(estado)) {
            System.err.println("Estado de lista no válido: " + estado);
            return false;
        }
        String sql = "DELETE FROM watchlist WHERE user_id = ? AND movie_id = ? AND status = ?";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, peliculaId);
            ps.setString(3, estado);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar de la lista: " + e.getMessage());
            return false;
        }
    }

    /**
     * Películas distintas presentes en cualquier estado de la lista del usuario.
     */
    /**
     * Todas las filas de lista del usuario (puede haber la misma película en favorito y en ver más tarde).
     */
    public List<EntradaLista> listarEntradasUsuario(int usuarioId) {
        List<EntradaLista> lista = new ArrayList<>();
        String sql = """
                SELECT w.watchlist_id, w.movie_id, m.title, w.status, w.added_at
                FROM watchlist w
                INNER JOIN movies m ON m.movie_id = w.movie_id
                WHERE w.user_id = ?
                ORDER BY w.added_at DESC
                """;
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new EntradaLista(
                            rs.getInt("watchlist_id"),
                            rs.getInt("movie_id"),
                            rs.getString("title"),
                            rs.getString("status"),
                            rs.getTimestamp("added_at")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar entradas de lista: " + e.getMessage());
        }
        return lista;
    }

    public List<Pelicula> obtenerListaPorUsuario(int usuarioId) {
        List<Pelicula> lista = new ArrayList<>();
        String sql = """
                SELECT DISTINCT m.movie_id, m.title, m.description, m.release_year, m.director, m.popularity
                FROM movies m
                INNER JOIN watchlist w ON m.movie_id = w.movie_id
                WHERE w.user_id = ?
                ORDER BY m.title
                """;
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(PeliculaDAO.mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener lista personal: " + e.getMessage());
        }
        return lista;
    }

    private static boolean esEstadoValido(String estado) {
        return Objects.equals(FAVORITE, estado) || Objects.equals(WATCH_LATER, estado);
    }
}
