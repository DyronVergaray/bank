package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * CAPA: MODEL — Acceso a Datos (DAO)
 * ============================================================
 * Responsable de toda la persistencia relacionada con
 * dbo.Notificacion y dbo.Preferencia_Notificacion.
 * Usa el SINGLETON ConexionBD. Llama solo a Stored Procedures.
 *
 * Clase: NotificacionDAO
 * Módulo: Notificaciones y Alertas — QoriBank
 * ============================================================
 */
public class NotificacionDAO {

    private Connection getConexion() {
        return ConexionBD.getInstancia().getConexion();
    }

    // --------------------------------------------------------
    // sp_GuardarPreferenciaNotificacion
    // --------------------------------------------------------
    public void guardarPreferencia(int idUsuario, String canal) {
        String sql = "{CALL sp_GuardarPreferenciaNotificacion(?, ?)}";
        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idUsuario);
            cs.setString(2, canal);
            cs.executeQuery();
        } catch (SQLException e) {
            System.err.println("[NotificacionDAO] Error en guardarPreferencia: " + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // sp_ObtenerPreferenciaNotificacion
    // Retorna el canal preferido o "CORREO" si no existe.
    // --------------------------------------------------------
    public String obtenerCanalPreferido(int idUsuario) {
        String sql = "{CALL sp_ObtenerPreferenciaNotificacion(?)}";
        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idUsuario);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return rs.getString("canal");
            }
        } catch (SQLException e) {
            System.err.println("[NotificacionDAO] Error en obtenerCanalPreferido: " + e.getMessage());
        }
        return "CORREO";
    }

    // --------------------------------------------------------
    // sp_CrearNotificacion
    // Retorna el id generado o -1 si hubo error.
    // --------------------------------------------------------
    public int crearNotificacion(Notificacion n) {
        String sql = "{CALL sp_CrearNotificacion(?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, n.getIdUsuario());
            cs.setInt(2, n.getTipo().getIdTipo());
            if (n.getIdTransferencia() != null)
                cs.setInt(3, n.getIdTransferencia());
            else
                cs.setNull(3, Types.INTEGER);
            cs.setString(4, n.getMensaje());
            cs.setString(5, n.getCanal());
            cs.setString(6, n.getEstado().name());
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return rs.getInt("id_notificacion");
            }
        } catch (SQLException e) {
            System.err.println("[NotificacionDAO] Error en crearNotificacion: " + e.getMessage());
        }
        return -1;
    }

    // --------------------------------------------------------
    // sp_ObtenerNotificacionesPorUsuario
    // --------------------------------------------------------
    public List<Notificacion> obtenerPorUsuario(int idUsuario) {
        List<Notificacion> lista = new ArrayList<>();
        String sql = "{CALL sp_ObtenerNotificacionesPorUsuario(?)}";
        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idUsuario);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("[NotificacionDAO] Error en obtenerPorUsuario: " + e.getMessage());
        }
        return lista;
    }

    // --------------------------------------------------------
    // sp_MarcarNotificacionLeida
    // --------------------------------------------------------
    public void marcarLeida(int idNotificacion) {
        String sql = "{CALL sp_MarcarNotificacionLeida(?)}";
        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idNotificacion);
            cs.executeQuery();
        } catch (SQLException e) {
            System.err.println("[NotificacionDAO] Error en marcarLeida: " + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // sp_MarcarTodasLeidas
    // --------------------------------------------------------
    public void marcarTodasLeidas(int idUsuario) {
        String sql = "{CALL sp_MarcarTodasLeidas(?)}";
        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idUsuario);
            cs.executeQuery();
        } catch (SQLException e) {
            System.err.println("[NotificacionDAO] Error en marcarTodasLeidas: " + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // sp_ContarNoLeidas
    // --------------------------------------------------------
    public int contarNoLeidas(int idUsuario) {
        String sql = "{CALL sp_ContarNoLeidas(?)}";
        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idUsuario);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return rs.getInt("no_leidas");
            }
        } catch (SQLException e) {
            System.err.println("[NotificacionDAO] Error en contarNoLeidas: " + e.getMessage());
        }
        return 0;
    }

    // --------------------------------------------------------
    // sp_ActualizarEstadoNotificacion
    // --------------------------------------------------------
    public void actualizarEstadoNotificacion(int idNotificacion, String nuevoEstado) {
        String sql = "{CALL sp_ActualizarEstadoNotificacion(?, ?)}";
        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idNotificacion);
            cs.setString(2, nuevoEstado);
            cs.executeQuery();
        } catch (SQLException e) {
            System.err.println("[NotificacionDAO] Error en actualizarEstadoNotificacion: " + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // sp_CancelarTransferenciaPendiente
    // --------------------------------------------------------
    public boolean cancelarTransferenciaPendiente(int idTransferencia, int idUsuario) {
        String sql = "{CALL sp_CancelarTransferenciaPendiente(?, ?)}";
        try (CallableStatement cs = getConexion().prepareCall(sql)) {
            cs.setInt(1, idTransferencia);
            cs.setInt(2, idUsuario);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return rs.getInt("canceladas") > 0;
            }
        } catch (SQLException e) {
            System.err.println("[NotificacionDAO] Error en cancelarTransferenciaPendiente: " + e.getMessage());
        }
        return false;
    }

    // --------------------------------------------------------
    // Mapeo ResultSet → Notificacion
    // --------------------------------------------------------
    private Notificacion mapear(ResultSet rs) throws SQLException {
        Notificacion n = new Notificacion();
        n.setIdNotificacion(rs.getInt("id_notificacion"));
        n.setMensaje(rs.getString("mensaje"));
        n.setCanal(rs.getString("canal"));
        n.setLeida(rs.getBoolean("leida"));

        int idTransf = rs.getInt("id_transferencia");
        if (!rs.wasNull()) n.setIdTransferencia(idTransf);

        String tipoStr = rs.getString("tipo");
        for (Notificacion.TipoNotificacion t : Notificacion.TipoNotificacion.values()) {
            if (t.name().equals(tipoStr)) { n.setTipo(t); break; }
        }

        String estadoStr = rs.getString("estado");
        for (Notificacion.EstadoNotificacion e : Notificacion.EstadoNotificacion.values()) {
            if (e.name().equals(estadoStr)) { n.setEstado(e); break; }
        }

        Timestamp ts = rs.getTimestamp("fecha_hora");
        if (ts != null) n.setFechaHora(ts.toLocalDateTime());

        return n;
    }
}
