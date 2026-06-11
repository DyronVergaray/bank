package model;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ============================================================
 * CAPA: MODEL — Acceso a Datos (DAO)
 * ============================================================
 * Responsable de todas las operaciones de persistencia relacionadas
 * con la tabla dbo.Usuario e dbo.Inicio_Sesion en SQL Server.
 *
 * Llama exclusivamente a Stored Procedures definidos en la BD,
 * garantizando separación entre lógica de negocio y SQL.
 *
 * Usa el SINGLETON ConexionBD para obtener la conexión.
 *
 * Clase: UsuarioDAO
 * ============================================================
 */
public class UsuarioDAO {

    // Obtiene la conexión única via SINGLETON
    private Connection getConexion() {
        return ConexionBD.getInstancia().getConexion();
    }

    // --------------------------------------------------------
    // Ejecuta sp_ValidarCredenciales
    // Retorna el objeto Usuario si las credenciales son válidas,
    // o null si no existe o está inactivo.
    // --------------------------------------------------------
    public Usuario validarCredenciales(String email, String passwordHash) {
        Usuario usuario = null;

        String sql = "{CALL sp_ValidarCredenciales(?, ?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setString(1, email);
            cs.setString(2, passwordHash);

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario();
                    usuario.setIdUsuario      (rs.getInt    ("id_usuario"));
                    usuario.setPrimerNombre   (rs.getString ("primer_nombre"));
                    usuario.setApellidoPaterno(rs.getString ("apellido_paterno"));
                    usuario.setApellidoMaterno(rs.getString ("apellido_materno"));
                    usuario.setEmail          (rs.getString ("email"));
                    usuario.setTelefono       (rs.getString ("telefono"));
                    usuario.setActivo         (rs.getBoolean("activo"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en validarCredenciales: " + e.getMessage());
        }

        return usuario;
    }

    // --------------------------------------------------------
    // Ejecuta sp_CrearSesionConCodigo
    // Registra la sesión pendiente y retorna el id_sesion generado.
    // Retorna -1 si ocurrió un error.
    // --------------------------------------------------------
    public int crearSesionConCodigo(int idUsuario, int idTipo, String codigoHash) {
        int idSesion = -1;

        String sql = "{CALL sp_CrearSesionConCodigo(?, ?, ?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt   (1, idUsuario);
            cs.setInt   (2, idTipo);
            cs.setString(3, codigoHash);

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    idSesion = rs.getInt("id_sesion");
                }
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en crearSesionConCodigo: " + e.getMessage());
        }

        return idSesion;
    }

    // --------------------------------------------------------
    // Ejecuta sp_VerificarCodigo
    // Retorna true si el código es correcto y la sesión fue activada.
    // --------------------------------------------------------
    public boolean verificarCodigo(int idSesion, String codigoHash, String tokenSesion) {
        boolean resultado = false;

        String sql = "{CALL sp_VerificarCodigo(?, ?, ?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt   (1, idSesion);
            cs.setString(2, codigoHash);
            cs.setString(3, tokenSesion);

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    resultado = rs.getBoolean("verificado");
                }
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en verificarCodigo: " + e.getMessage());
        }

        return resultado;
    }

    // --------------------------------------------------------
    // Ejecuta sp_ObtenerUsuarioPorSesion
    // Retorna el Usuario activo correspondiente al token de sesión.
    // Retorna null si el token es inválido o expiró.
    // --------------------------------------------------------
    public Usuario obtenerUsuarioPorSesion(String tokenSesion) {
        Usuario usuario = null;

        String sql = "{CALL sp_ObtenerUsuarioPorSesion(?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setString(1, tokenSesion);

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario();
                    usuario.setIdUsuario      (rs.getInt    ("id_usuario"));
                    usuario.setPrimerNombre   (rs.getString ("primer_nombre"));
                    usuario.setApellidoPaterno(rs.getString ("apellido_paterno"));
                    usuario.setApellidoMaterno(rs.getString ("apellido_materno"));
                    usuario.setEmail          (rs.getString ("email"));
                    usuario.setTelefono       (rs.getString ("telefono"));
                    usuario.setActivo         (rs.getBoolean("activo"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en obtenerUsuarioPorSesion: " + e.getMessage());
        }

        return usuario;
    }
}
