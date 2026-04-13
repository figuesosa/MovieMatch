package moviematch.dao;

import moviematch.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Preferencias de género del usuario (tabla {@code preferences}).
 */
public class PreferenciaDAO {

    public boolean agregarPreferencia(int usuarioId, int generoId) {
        String sql = "INSERT INTO preferences (user_id, genre_id) VALUES (?, ?)";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, generoId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al agregar preferencia: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarPreferencia(int usuarioId, int generoId) {
        String sql = "DELETE FROM preferences WHERE user_id = ? AND genre_id = ?";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, generoId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar preferencia: " + e.getMessage());
            return false;
        }
    }

    /**
     * Identificadores de género asociados al usuario.
     */
    public List<Integer> obtenerPreferenciasPorUsuario(int usuarioId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT genre_id FROM preferences WHERE user_id = ? ORDER BY genre_id";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("genre_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener preferencias: " + e.getMessage());
        }
        return ids;
    }
}
