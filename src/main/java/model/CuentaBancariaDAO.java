package model;

import java.math.BigDecimal;
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
 * Responsable de toda la persistencia relacionada con
 * dbo.Cuenta_Bancaria y dbo.Tipo_Cuenta_Banco en SQL Server.
 *
 * Llama exclusivamente a Stored Procedures, igual que UsuarioDAO.
 * Usa el SINGLETON ConexionBD para obtener la conexión.
 *
 * Clase: CuentaBancariaDAO
 * Módulo: Vinculación de Cuentas Bancarias — QoriBank
 * ============================================================
 */
public class CuentaBancariaDAO {

    private Connection getConexion() {
        return ConexionBD.getInstancia().getConexion();
    }

    // --------------------------------------------------------
    // sp_ExisteTarjeta
    // Retorna true si el número de tarjeta (hash) ya pertenece
    // a algún usuario en el sistema.
    // --------------------------------------------------------
    public boolean existeTarjeta(String numeroTarjetaHash) {
        boolean existe = false;
        String sql = "{CALL sp_ExisteTarjeta(?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setString(1, numeroTarjetaHash);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) existe = rs.getInt("existe") > 0;
            }
        } catch (SQLException e) {
            System.err.println("[CuentaBancariaDAO] Error en existeTarjeta: " + e.getMessage());
        }
        return existe;
    }

    // --------------------------------------------------------
    // sp_RegistrarCuentaBancaria
    // Retorna:
    //   id > 0  → registrada correctamente (estado PENDIENTE)
    //   -1      → la tarjeta ya está registrada
    //   -99     → error técnico
    // --------------------------------------------------------
    public int registrarCuentaBancaria(int idUsuario, int idTipoCuenta,
                                       String numeroTarjetaHash, String numeroTarjetaEnmascarado,
                                       String fechaVencimiento, String cvvHash,
                                       BigDecimal saldoInicial) {
        String sql = "{CALL sp_RegistrarCuentaBancaria(?, ?, ?, ?, ?, ?, ?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt       (1, idUsuario);
            cs.setInt       (2, idTipoCuenta);
            cs.setString    (3, numeroTarjetaHash);
            cs.setString    (4, numeroTarjetaEnmascarado);
            cs.setString    (5, fechaVencimiento);
            cs.setString    (6, cvvHash);
            cs.setBigDecimal(7, saldoInicial);

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_cuenta_bancaria");
                }
            }
        } catch (SQLException e) {
            System.err.println("[CuentaBancariaDAO] Error en registrarCuentaBancaria: " + e.getMessage());
        }
        return -99;
    }

    // --------------------------------------------------------
    // sp_ActualizarEstadoCuentaBancaria
    // --------------------------------------------------------
    public boolean actualizarEstadoCuentaBancaria(int idCuentaBancaria, String nuevoEstado) {
        boolean ok = false;
        String sql = "{CALL sp_ActualizarEstadoCuentaBancaria(?, ?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt   (1, idCuentaBancaria);
            cs.setString(2, nuevoEstado);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) ok = rs.getInt("actualizados") > 0;
            }
        } catch (SQLException e) {
            System.err.println("[CuentaBancariaDAO] Error en actualizarEstadoCuentaBancaria: " + e.getMessage());
        }
        return ok;
    }

    // --------------------------------------------------------
    // sp_ObtenerCuentasPorUsuario
    // Retorna la lista de cuentas bancarias vinculadas (o
    // pendientes) de un usuario.
    // --------------------------------------------------------
    public List<CuentaBancaria> obtenerCuentasPorUsuario(int idUsuario) {
        List<CuentaBancaria> lista = new ArrayList<>();
        String sql = "{CALL sp_ObtenerCuentasPorUsuario(?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idUsuario);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    CuentaBancaria c = new CuentaBancaria();
                    c.setIdCuentaBancaria(rs.getInt("id_cuenta_bancaria"));
                    c.setIdTipoCuenta(rs.getInt("id_tipo_cuenta"));
                    c.setNombreEntidad(rs.getString("nombre_entidad"));
                    c.setNumeroTarjetaEnmascarado(rs.getString("numero_tarjeta_enmascarado"));
                    c.setFechaVencimiento(rs.getString("fecha_vencimiento"));
                    c.setEstado(CuentaBancaria.EstadoCuenta.valueOf(rs.getString("estado")));
                    c.setSaldo(rs.getBigDecimal("saldo"));
                    lista.add(c);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CuentaBancariaDAO] Error en obtenerCuentasPorUsuario: " + e.getMessage());
        }
        return lista;
    }

    // --------------------------------------------------------
    // sp_ObtenerCuentaBancariaPorId
    // Incluye endpoint/api_key de la entidad (para el Adapter).
    // --------------------------------------------------------
    public CuentaBancaria obtenerCuentaBancariaPorId(int idCuentaBancaria) {
        CuentaBancaria c = null;
        String sql = "{CALL sp_ObtenerCuentaBancariaPorId(?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idCuentaBancaria);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    c = new CuentaBancaria();
                    c.setIdCuentaBancaria(rs.getInt("id_cuenta_bancaria"));
                    c.setIdUsuario(rs.getInt("id_usuario"));
                    c.setIdTipoCuenta(rs.getInt("id_tipo_cuenta"));
                    c.setNombreEntidad(rs.getString("nombre_entidad"));
                    c.setNumeroTarjetaEnmascarado(rs.getString("numero_tarjeta_enmascarado"));
                    c.setFechaVencimiento(rs.getString("fecha_vencimiento"));
                    c.setEstado(CuentaBancaria.EstadoCuenta.valueOf(rs.getString("estado")));
                    c.setSaldo(rs.getBigDecimal("saldo"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[CuentaBancariaDAO] Error en obtenerCuentaBancariaPorId: " + e.getMessage());
        }
        return c;
    }

    // --------------------------------------------------------
    // sp_EliminarCuentaBancariaPendiente
    // Solo elimina cuentas en estado PENDIENTE o ERROR.
    // --------------------------------------------------------
    public boolean eliminarCuentaBancariaPendiente(int idCuentaBancaria) {
        boolean ok = false;
        String sql = "{CALL sp_EliminarCuentaBancariaPendiente(?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idCuentaBancaria);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) ok = rs.getInt("eliminados") > 0;
            }
        } catch (SQLException e) {
            System.err.println("[CuentaBancariaDAO] Error en eliminarCuentaBancariaPendiente: " + e.getMessage());
        }
        return ok;
    }

    // --------------------------------------------------------
    // sp_CrearSesionVinculacionCuenta
    // Crea la sesión OTP asociada a una cuenta bancaria pendiente.
    // --------------------------------------------------------
    public int crearSesionVinculacionCuenta(int idUsuario, int idTipo,
                                            int idCuentaBancaria, String codigoHash) {
        int idSesion = -1;
        String sql = "{CALL sp_CrearSesionVinculacionCuenta(?, ?, ?, ?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt   (1, idUsuario);
            cs.setInt   (2, idTipo);
            cs.setInt   (3, idCuentaBancaria);
            cs.setString(4, codigoHash);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) idSesion = rs.getInt("id_sesion");
            }
        } catch (SQLException e) {
            System.err.println("[CuentaBancariaDAO] Error en crearSesionVinculacionCuenta: " + e.getMessage());
        }
        return idSesion;
    }

    // --------------------------------------------------------
    // sp_VerificarCodigoVinculacion
    // Retorna el id de cuenta bancaria si el código fue correcto,
    // o -1 si fue incorrecto/expiró.
    // --------------------------------------------------------
    public int verificarCodigoVinculacion(int idSesion, String codigoHash) {
        String sql = "{CALL sp_VerificarCodigoVinculacion(?, ?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt   (1, idSesion);
            cs.setString(2, codigoHash);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    boolean valido = rs.getBoolean("verificado");
                    if (valido) return rs.getInt("id_cuenta_bancaria");
                }
            }
        } catch (SQLException e) {
            System.err.println("[CuentaBancariaDAO] Error en verificarCodigoVinculacion: " + e.getMessage());
        }
        return -1;
    }

    // --------------------------------------------------------
    // sp_ObtenerTiposCuentaBanco
    // Lista las entidades bancarias activas disponibles.
    // --------------------------------------------------------
    public List<TipoCuentaBancaria> obtenerTiposCuentaBanco() {
        List<TipoCuentaBancaria> lista = new ArrayList<>();
        String sql = "{CALL sp_ObtenerTiposCuentaBanco()}";

        try (CallableStatement cs = getConexion().prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                TipoCuentaBancaria t = new TipoCuentaBancaria();
                t.setIdTipoCuenta(rs.getInt("id_tipo_cuenta"));
                t.setNombreEntidad(rs.getString("nombre_entidad"));
                t.setApiEndpoint(rs.getString("api_endpoint"));
                t.setApiKey(rs.getString("api_key"));
                t.setActivo(rs.getBoolean("activo"));
                lista.add(t);
            }
        } catch (SQLException e) {
            System.err.println("[CuentaBancariaDAO] Error en obtenerTiposCuentaBanco: " + e.getMessage());
        }
        return lista;
    }

    // --------------------------------------------------------
    // sp_CrearTipoCuentaBanco   (Admin)
    // Retorna id > 0 si fue exitoso, -1 si el nombre ya existe.
    // --------------------------------------------------------
    public int crearTipoCuentaBanco(String nombreEntidad, String apiEndpoint, String apiKey) {
        String sql = "{CALL sp_CrearTipoCuentaBanco(?, ?, ?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setString(1, nombreEntidad);
            cs.setString(2, apiEndpoint);
            cs.setString(3, apiKey);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return rs.getInt("id_tipo_cuenta");
            }
        } catch (SQLException e) {
            System.err.println("[CuentaBancariaDAO] Error en crearTipoCuentaBanco: " + e.getMessage());
        }
        return -99;
    }

    // --------------------------------------------------------
    // sp_EliminarTipoCuentaBanco   (Admin)
    // Retorna true si se eliminó o desactivó correctamente.
    // --------------------------------------------------------
    public boolean eliminarTipoCuentaBanco(int idTipoCuenta) {
        boolean ok = false;
        String sql = "{CALL sp_EliminarTipoCuentaBanco(?)}";

        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idTipoCuenta);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) ok = rs.getInt("eliminados") > 0;
            }
        } catch (SQLException e) {
            System.err.println("[CuentaBancariaDAO] Error en eliminarTipoCuentaBanco: " + e.getMessage());
        }
        return ok;
    }
}
