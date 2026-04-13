package moviematch.dao;

import moviematch.modelo.Genero;
import moviematch.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la tabla {@code genres}.
 */
public class GeneroDAO {

    public Genero obtenerGeneroPorId(int id) {
        String sql = "SELECT genre_id, name FROM genres WHERE genre_id = ?";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Genero(rs.getInt("genre_id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener genero: " + e.getMessage());
        }
        return null;
    }

    public Genero obtenerGeneroPorNombreExacto(String nombre) {
        String sql = "SELECT genre_id, name FROM genres WHERE name = ?";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Genero(rs.getInt("genre_id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener genero por nombre: " + e.getMessage());
        }
        return null;
    }

    /**
     * Crea el genero y devuelve el id generado; -1 si falla.
     */
    public int crearGenero(String nombre) {
        String sql = "INSERT INTO genres (name) VALUES (?)";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al crear genero: " + e.getMessage());
        }
        return -1;
    }

    public List<Genero> obtenerTodosLosGeneros() {
        List<Genero> lista = new ArrayList<>();
        String sql = "SELECT genre_id, name FROM genres ORDER BY name";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Genero(rs.getInt("genre_id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar generos: " + e.getMessage());
        }
        return lista;
    }
}
