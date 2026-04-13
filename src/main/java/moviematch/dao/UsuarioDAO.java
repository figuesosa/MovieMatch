package moviematch.dao;

import moviematch.modelo.Usuario;
import moviematch.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la tabla {@code users}. Usa {@link PreparedStatement} para evitar inyección SQL.
 */
public class UsuarioDAO {

    /**
     * Inserta un nuevo usuario. No asigna {@code user_id}; la base lo genera.
     *
     * @return {@code true} si se insertó al menos una fila
     */
    public boolean crearUsuario(Usuario usuario) {
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getContrasena());
            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        usuario.setId(rs.getInt(1));
                    }
                }
            }
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear usuario: " + e.getMessage());
            return false;
        }
    }

    public Usuario obtenerUsuarioPorId(int id) {
        String sql = "SELECT user_id, username, email, password, created_at FROM users WHERE user_id = ?";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario por id: " + e.getMessage());
        }
        return null;
    }

    public Usuario obtenerUsuarioPorNombre(String nombreUsuario) {
        String sql = "SELECT user_id, username, email, password, created_at FROM users WHERE username = ?";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario por nombre: " + e.getMessage());
        }
        return null;
    }

    public List<Usuario> obtenerTodosLosUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT user_id, username, email, password, created_at FROM users ORDER BY user_id";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return lista;
    }

    public boolean actualizarUsuario(Usuario usuario) {
        String sql = "UPDATE users SET username = ?, email = ?, password = ? WHERE user_id = ?";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getContrasena());
            ps.setInt(4, usuario.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarUsuario(int id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Comprueba si existe un usuario con el nombre y la contraseña indicados.
     */
    public boolean existeNombreUsuario(String nombreUsuario) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al comprobar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean existeEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al comprobar email: " + e.getMessage());
            return false;
        }
    }

    public boolean validarLogin(String nombreUsuario, String contrasena) {
        String sql = "SELECT 1 FROM users WHERE username = ? AND password = ?";
        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            ps.setString(2, contrasena);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al validar login: " + e.getMessage());
            return false;
        }
    }

    private static Usuario mapear(ResultSet rs) throws SQLException {
        int id = rs.getInt("user_id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String password = rs.getString("password");
        Timestamp created = rs.getTimestamp("created_at");
        return new Usuario(id, username, email, password, created);
    }
}
