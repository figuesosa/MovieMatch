package moviematch.dao;

import moviematch.modelo.Pelicula;
import moviematch.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para películas ({@code movies}) y su relación con géneros ({@code movie_genres}).
 */
public class PeliculaDAO {

    public boolean agregarPelicula(Pelicula pelicula) {
        String sql = """
                INSERT INTO movies (title, description, release_year, director, popularity, poster_url)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, pelicula.getTitulo());
            ps.setString(2, pelicula.getDescripcion());
            if (pelicula.getAnioLanzamiento() > 0) {
                ps.setInt(3, pelicula.getAnioLanzamiento());
            } else {
                ps.setObject(3, null);
            }
            ps.setString(4, pelicula.getDirector());
            ps.setDouble(5, pelicula.getPopularidad());
            ps.setString(6, pelicula.getPosterUrl());
            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        pelicula.setId(rs.getInt(1));
                    }
                }
            }
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("Error al agregar película: " + e.getMessage());
            return false;
        }
    }

    public Pelicula obtenerPeliculaPorId(int id) {
        String sql = """
                SELECT movie_id, title, description, release_year, director, popularity, poster_url
                FROM movies WHERE movie_id = ?
                """;
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener película por id: " + e.getMessage());
        }
        return null;
    }

    public List<Pelicula> obtenerTodasLasPeliculas() {
        List<Pelicula> lista = new ArrayList<>();
        String sql = """
                SELECT movie_id, title, description, release_year, director, popularity, poster_url
                FROM movies ORDER BY title
                """;
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar películas: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Búsqueda por título o descripción (coincidencia parcial, sin distinguir mayúsculas).
     */
    public List<Pelicula> buscarPeliculas(String palabraClave) {
        List<Pelicula> lista = new ArrayList<>();
        if (palabraClave == null || palabraClave.isBlank()) {
            return lista;
        }
        String patron = "%" + palabraClave.trim() + "%";
        String sql = """
                SELECT movie_id, title, description, release_year, director, popularity, poster_url
                FROM movies
                WHERE LOWER(title) LIKE LOWER(?) OR LOWER(description) LIKE LOWER(?)
                ORDER BY title
                """;
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar películas: " + e.getMessage());
        }
        return lista;
    }

    public boolean actualizarPelicula(Pelicula pelicula) {
        String sql = """
                UPDATE movies SET title = ?, description = ?, release_year = ?, director = ?, popularity = ?, poster_url = ?
                WHERE movie_id = ?
                """;
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pelicula.getTitulo());
            ps.setString(2, pelicula.getDescripcion());
            if (pelicula.getAnioLanzamiento() > 0) {
                ps.setInt(3, pelicula.getAnioLanzamiento());
            } else {
                ps.setObject(3, null);
            }
            ps.setString(4, pelicula.getDirector());
            ps.setDouble(5, pelicula.getPopularidad());
            ps.setString(6, pelicula.getPosterUrl());
            ps.setInt(7, pelicula.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar película: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarPelicula(int id) {
        String sql = "DELETE FROM movies WHERE movie_id = ?";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar película: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarPosterUrl(int movieId, String posterUrl) {
        String sql = "UPDATE movies SET poster_url = ? WHERE movie_id = ?";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, posterUrl);
            ps.setInt(2, movieId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar poster_url: " + e.getMessage());
            return false;
        }
    }

    /**
     * Identificadores de género asociados a la película (tabla {@code movie_genres}).
     */
    public List<Integer> obtenerIdsGenerosDePelicula(int movieId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT genre_id FROM movie_genres WHERE movie_id = ? ORDER BY genre_id";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("genre_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al leer géneros de la película: " + e.getMessage());
        }
        return ids;
    }

    /**
     * Sustituye los géneros de una película (transacción).
     */
    public boolean reemplazarGenerosPelicula(int movieId, List<Integer> genreIds) {
        try (Connection c = ConexionBD.getConexion()) {
            c.setAutoCommit(false);
            try {
                String del = "DELETE FROM movie_genres WHERE movie_id = ?";
                try (PreparedStatement ps = c.prepareStatement(del)) {
                    ps.setInt(1, movieId);
                    ps.executeUpdate();
                }
                String ins = "INSERT INTO movie_genres (movie_id, genre_id) VALUES (?, ?)";
                try (PreparedStatement ps = c.prepareStatement(ins)) {
                    for (Integer gid : genreIds) {
                        if (gid == null) {
                            continue;
                        }
                        ps.setInt(1, movieId);
                        ps.setInt(2, gid);
                        ps.executeUpdate();
                    }
                }
                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                System.err.println("Error al reemplazar géneros: " + e.getMessage());
                return false;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error de conexión al reemplazar géneros: " + e.getMessage());
            return false;
        }
    }

    public List<Pelicula> obtenerPeliculasPorGenero(int generoId) {
        List<Pelicula> lista = new ArrayList<>();
        String sql = """
                SELECT m.movie_id, m.title, m.description, m.release_year, m.director, m.popularity, m.poster_url
                FROM movies m
                INNER JOIN movie_genres mg ON m.movie_id = mg.movie_id
                WHERE mg.genre_id = ?
                ORDER BY m.title
                """;
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, generoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener películas por género: " + e.getMessage());
        }
        return lista;
    }

    static Pelicula mapear(ResultSet rs) throws SQLException {
        int id = rs.getInt("movie_id");
        String titulo = rs.getString("title");
        String desc = rs.getString("description");
        int anio = rs.getObject("release_year") == null ? 0 : rs.getInt("release_year");
        String director = rs.getString("director");
        double pop = rs.getDouble("popularity");
        String posterUrl = rs.getString("poster_url");
        return new Pelicula(id, titulo, desc, anio, director, pop, posterUrl);
    }
}
