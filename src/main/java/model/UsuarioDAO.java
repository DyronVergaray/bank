package model;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * CAPA: MODEL — Acceso a Datos (DAO)
 * ============================================================
 * Responsable de todas las operaciones de persistencia relacionadas
 * con dbo.Usuario e dbo.Inicio_Sesion en SQL Server.
 *
 * Llama exclusivamente a Stored Procedures definidos en la BD.
 * Usa el SINGLETON ConexionBD para obtener la conexión.
 *
 * Clase: UsuarioDAO
 * Módulo: Gestión de Usuarios y Autenticación — QoriBank
 * ============================================================
 */
public class UsuarioDAO {

    private Connection getConexion() {
        return ConexionBD.getInstancia().getConexion();
    }

    // --------------------------------------------------------
    // Mapea un ResultSet a un objeto Usuario (campos comunes)
    // --------------------------------------------------------
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario      (rs.getInt    ("id_usuario"));
        u.setPrimerNombre   (rs.getString ("primer_nombre"));
        u.setApellidoPaterno(rs.getString ("apellido_paterno"));
        u.setApellidoMaterno(rs.getString ("apellido_materno"));
        u.setEmail          (rs.getString ("email"));
        u.setTelefono       (rs.getString ("telefono"));
        u.setRol            (rs.getString ("rol"));
        u.setActivo         (rs.getBoolean("activo"));
        return u;
    }

    // --------------------------------------------------------
    // sp_ValidarCredenciales
    // Retorna Usuario si las credenciales son válidas, o null.
    // --------------------------------------------------------
    public Usuario validarCredenciales(String email, String passwordHash) {
        Usuario usuario = null;
        String sql = "{CALL sp_ValidarCredenciales(?, ?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setString(1, email);
            cs.setString(2, passwordHash);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) usuario = mapearUsuario(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en validarCredenciales: " + e.getMessage());
        }
        return usuario;
    }

    // --------------------------------------------------------
    // sp_CrearSesionConCodigo
    // Retorna el id_sesion generado, o -1 si hubo error.
    // --------------------------------------------------------
    public int crearSesionConCodigo(int idUsuario, int idTipo, String codigoHash) {
        int idSesion = -1;
        String sql = "{CALL sp_CrearSesionConCodigo(?, ?, ?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt   (1, idUsuario);
            cs.setInt   (2, idTipo);
            cs.setString(3, codigoHash);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) idSesion = rs.getInt("id_sesion");
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en crearSesionConCodigo: " + e.getMessage());
        }
        return idSesion;
    }

    // --------------------------------------------------------
    // sp_VerificarCodigo
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
                if (rs.next()) resultado = rs.getBoolean("verificado");
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en verificarCodigo: " + e.getMessage());
        }
        return resultado;
    }

    // --------------------------------------------------------
    // sp_ObtenerUsuarioPorSesion
    // Retorna el Usuario activo para el token dado, o null.
    // --------------------------------------------------------
    public Usuario obtenerUsuarioPorSesion(String tokenSesion) {
        Usuario usuario = null;
        String sql = "{CALL sp_ObtenerUsuarioPorSesion(?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setString(1, tokenSesion);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) usuario = mapearUsuario(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en obtenerUsuarioPorSesion: " + e.getMessage());
        }
        return usuario;
    }

    // --------------------------------------------------------
    // sp_ObtenerTodosUsuarios   (Admin)
    // Retorna la lista completa de usuarios del sistema.
    // --------------------------------------------------------
    public List<Usuario> obtenerTodosUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "{CALL sp_ObtenerTodosUsuarios()}";

        try (CallableStatement cs = getConexion().prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) lista.add(mapearUsuario(rs));
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en obtenerTodosUsuarios: " + e.getMessage());
        }
        return lista;
    }

    // --------------------------------------------------------
    // sp_EliminarUsuario   (Admin)
    // Retorna true si se eliminó al menos un registro.
    // --------------------------------------------------------
    public boolean eliminarUsuario(int idUsuario) {
        boolean ok = false;
        String sql = "{CALL sp_EliminarUsuario(?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idUsuario);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) ok = rs.getInt("eliminados") > 0;
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en eliminarUsuario: " + e.getMessage());
        }
        return ok;
    }

    // --------------------------------------------------------
    // sp_CrearUsuarioCliente   (Registro público)
    // Retorna:
    //   id > 0  → creado correctamente
    //   -1      → email duplicado
    //   -2      → teléfono duplicado
    //   -99     → error técnico
    // --------------------------------------------------------
    public int crearUsuarioCliente(String primerNombre, String apellidoPaterno,
                                   String apellidoMaterno, String email,
                                   String telefono, String passwordHash) {
        return ejecutarCrearUsuario("sp_CrearUsuarioCliente",
                primerNombre, apellidoPaterno, apellidoMaterno,
                email, telefono, passwordHash);
    }

    // --------------------------------------------------------
    // sp_CrearUsuarioAdmin   (Panel Admin)
    // Mismos códigos de retorno que crearUsuarioCliente.
    // --------------------------------------------------------
    public int crearUsuarioAdmin(String primerNombre, String apellidoPaterno,
                                 String apellidoMaterno, String email,
                                 String telefono, String passwordHash) {
        return ejecutarCrearUsuario("sp_CrearUsuarioAdmin",
                primerNombre, apellidoPaterno, apellidoMaterno,
                email, telefono, passwordHash);
    }

    // --------------------------------------------------------
    // Método privado compartido para ambos SPs de creación
    // --------------------------------------------------------
    private int ejecutarCrearUsuario(String sp,
                                     String primerNombre, String apellidoPaterno,
                                     String apellidoMaterno, String email,
                                     String telefono, String passwordHash) {
        String sql = "{CALL " + sp + "(?, ?, ?, ?, ?, ?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setString(1, primerNombre);
            cs.setString(2, apellidoPaterno);
            cs.setString(3, apellidoMaterno);
            cs.setString(4, email);
            cs.setString(5, telefono);
            cs.setString(6, passwordHash);

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt("id_usuario");
                    String mensaje = rs.getString("mensaje");
                    System.out.println("[UsuarioDAO] " + sp + " → id=" + idGenerado + " msg=" + mensaje);
                    return idGenerado;   // positivo = éxito, negativo = error de negocio
                }
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en " + sp + ": " + e.getMessage());
        }
        return -99;
    }
}
