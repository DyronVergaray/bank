package model;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * CAPA: MODEL — Acceso a Datos (DAO)
 * ============================================================
 * Responsable de toda la persistencia relacionada con
 * dbo.Transferencia en SQL Server.
 *
 * Usa el SINGLETON ConexionBD. Llama solo a Stored Procedures.
 *
 * Clase: TransferenciaDAO
 * Módulo: Transferencias — QoriBank
 * ============================================================
 */
public class TransferenciaDAO {

    private Connection getConexion() {
        return ConexionBD.getInstancia().getConexion();
    }

    // --------------------------------------------------------
    // sp_ValidarFondos
    // Retorna true si la cuenta tiene saldo suficiente.
    // --------------------------------------------------------
    public boolean validarFondos(int idCuentaOrigen, BigDecimal monto) {
        String sql = "{CALL sp_ValidarFondos(?, ?)}";
        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idCuentaOrigen);
            cs.setBigDecimal(2, monto);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return rs.getInt("fondos_suficientes") == 1;
            }
        } catch (SQLException e) {
            System.err.println("[TransferenciaDAO] Error en validarFondos: " + e.getMessage());
        }
        return false;
    }

    // --------------------------------------------------------
    // sp_RegistrarTransferencia
    // Inserta la transferencia en estado PENDIENTE.
    // Retorna el id generado o -1 si hubo error.
    // --------------------------------------------------------
    public int registrarTransferencia(int idUsuario, int idTipo,
                                      int idCuentaOrigen, String origenEnm,
                                      String destinoHash, String destinoEnm,
                                      String entidadDestino, BigDecimal monto,
                                      String descripcion) {
        String sql = "{CALL sp_RegistrarTransferencia(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idUsuario);
            cs.setInt(2, idTipo);
            cs.setInt(3, idCuentaOrigen);
            cs.setString(4, origenEnm);
            cs.setString(5, destinoHash);
            cs.setString(6, destinoEnm);
            cs.setString(7, entidadDestino);
            cs.setBigDecimal(8, monto);
            cs.setString(9, descripcion);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return rs.getInt("id_transferencia");
            }
        } catch (SQLException e) {
            System.err.println("[TransferenciaDAO] Error en registrarTransferencia: " + e.getMessage());
        }
        return -1;
    }

    // --------------------------------------------------------
    // sp_CrearSesionTransferencia
    // Crea el código OTP vinculado a la transferencia pendiente.
    // Retorna el id_sesion o -1.
    // --------------------------------------------------------
    public int crearSesionTransferencia(int idUsuario, int idTipo,
                                        int idTransferencia, String codigoHash) {
        String sql = "{CALL sp_CrearSesionTransferencia(?, ?, ?, ?)}";
        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idUsuario);
            cs.setInt(2, idTipo);
            cs.setInt(3, idTransferencia);
            cs.setString(4, codigoHash);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return rs.getInt("id_sesion");
            }
        } catch (SQLException e) {
            System.err.println("[TransferenciaDAO] Error en crearSesionTransferencia: " + e.getMessage());
        }
        return -1;
    }

    // --------------------------------------------------------
    // sp_ProcesarTransferencia
    // Verifica OTP, descuenta/acredita saldos, actualiza estado.
    // Retorna true si fue exitosa.
    // --------------------------------------------------------
    public boolean procesarTransferencia(int idTransferencia,
                                         String codigoHash, int idSesion) {
        String sql = "{CALL sp_ProcesarTransferencia(?, ?, ?)}";
        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idTransferencia);
            cs.setString(2, codigoHash);
            cs.setInt(3, idSesion);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return rs.getInt("exitosa") == 1;
            }
        } catch (SQLException e) {
            System.err.println("[TransferenciaDAO] Error en procesarTransferencia: " + e.getMessage());
        }
        return false;
    }

    // --------------------------------------------------------
    // sp_ObtenerHistorialTransferencias
    // Lista todo el historial del usuario.
    // --------------------------------------------------------
    public List<Transferencia> obtenerHistorial(int idUsuario) {
        List<Transferencia> lista = new ArrayList<>();
        String sql = "{CALL sp_ObtenerHistorialTransferencias(?)}";
        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idUsuario);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearTransferencia(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TransferenciaDAO] Error en obtenerHistorial: " + e.getMessage());
        }
        return lista;
    }

    // --------------------------------------------------------
    // sp_ObtenerTransferenciaPorId
    // Para generar el comprobante.
    // --------------------------------------------------------
    public Transferencia obtenerPorId(int idTransferencia) {
        String sql = "{CALL sp_ObtenerTransferenciaPorId(?)}";
        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idTransferencia);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return mapearTransferencia(rs);
            }
        } catch (SQLException e) {
            System.err.println("[TransferenciaDAO] Error en obtenerPorId: " + e.getMessage());
        }
        return null;
    }

    // --------------------------------------------------------
    // Mapeo ResultSet → Transferencia
    // --------------------------------------------------------
    private Transferencia mapearTransferencia(ResultSet rs) throws SQLException {
        Transferencia t = new Transferencia();
        t.setIdTransferencia(rs.getInt("id_transferencia"));
        t.setNumeroTarjetaOrigenEnmascarado(rs.getString("numero_tarjeta_origen_enmascarado"));
        t.setNumeroTarjetaDestinoEnmascarado(rs.getString("numero_tarjeta_destino_enmascarado"));
        t.setEntidadDestino(rs.getString("entidad_destino"));
        t.setMonto(rs.getBigDecimal("monto"));
        t.setDescripcion(rs.getString("descripcion"));
        t.setEstado(Transferencia.EstadoTransferencia.valueOf(rs.getString("estado")));

        String tipoStr = rs.getString("tipo");
        for (Transferencia.TipoTransferencia tt : Transferencia.TipoTransferencia.values()) {
            if (tt.name().equals(tipoStr)) { t.setTipo(tt); break; }
        }

        Timestamp creadoEn = rs.getTimestamp("creado_en");
        if (creadoEn != null) t.setCreadoEn(creadoEn.toLocalDateTime());

        Timestamp procesadoEn = rs.getTimestamp("procesado_en");
        if (procesadoEn != null) t.setProcesadoEn(procesadoEn.toLocalDateTime());

        return t;
    }
}
