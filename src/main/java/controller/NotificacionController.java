package controller;

import model.*;
import util.SeguridadUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * ============================================================
 * CAPA: CONTROLLER — Notificaciones y Alertas
 * ============================================================
 * Coordina el flujo completo del Módulo 6:
 *   - Guardar/obtener preferencia de canal del usuario
 *   - Generar notificaciones automáticas (sesión, transferencias)
 *   - Gestionar alertas de seguridad (monto >= S/500):
 *       crear alerta → OTP → confirmar/cancelar
 *   - Listar y marcar notificaciones como leídas
 *
 * Patrones en uso:
 *   - SINGLETON  → ConfiguracionNotificacion (vía NotificacionFacade)
 *   - FACTORY    → ProveedorNotificacionAdapter (vía NotificacionFacade)
 *   - ADAPTER    → ProveedorSMS/Correo/Push
 *   - FACADE     → NotificacionFacade (orquesta todo el envío)
 *   - STATE      → Notificacion.EstadoNotificacion
 *   - PROTOTYPE  → Notificacion.plantilla*()
 *   - OBSERVER   → eventos automáticos del sistema
 *
 * Clase: NotificacionController
 * Módulo: Notificaciones y Alertas — QoriBank
 * ============================================================
 */
public class NotificacionController {

    private final NotificacionFacade facade;
    private final Usuario             usuarioActual;

    // Estado de la alerta en confirmación
    private Notificacion alertaPendiente;
    private int          idSesionAlerta;
    private String       codigoGenerado;

    public NotificacionController(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        this.facade        = new NotificacionFacade();
    }

    // ============================================================
    // PREFERENCIA DE CANAL
    // ============================================================

    public void guardarPreferencia(String canal) {
        facade.guardarPreferencia(usuarioActual.getIdUsuario(), canal);
    }

    public String obtenerCanalPreferido() {
        return facade.obtenerCanalPreferido(usuarioActual.getIdUsuario());
    }

    // ============================================================
    // NOTIFICACIONES AUTOMÁTICAS (llamadas desde otros módulos)
    // ============================================================

    /** Llamado por AutenticacionController tras login exitoso. */
    public void notificarInicioSesion() {
        if (usuarioActual == null) return;
        facade.notificarInicioSesion(usuarioActual);
    }

    /** Llamado por TransferenciaController tras transferencia exitosa. */
    public void notificarTransferenciaEfectuada(Transferencia t) {
        if (usuarioActual == null) return;
        facade.notificarTransferenciaEfectuada(usuarioActual, t);
    }

    /**
     * Llamado por TransferenciaController cuando la tarjeta destino
     * pertenece a un usuario del sistema.
     */
    public void notificarTransferenciaRecibida(Usuario destinatario, Transferencia t) {
        if (destinatario == null) return;
        facade.notificarTransferenciaRecibida(destinatario, t);
    }

    // ============================================================
    // ALERTA DE SEGURIDAD (monto >= S/ 500)
    // ============================================================

    /**
     * Verifica si la transferencia supera el umbral de alerta (S/500).
     */
    public boolean requiereAlerta(BigDecimal monto) {
        return monto != null &&
               monto.compareTo(ConfiguracionNotificacion.UMBRAL_ALERTA) >= 0;
    }

    /**
     * Crea la alerta de seguridad para una transferencia >= S/500.
     * La transferencia queda en PENDIENTE hasta que el usuario confirme.
     * Retorna el id de la notificación creada.
     */
    public int crearAlertaSeguridad(Transferencia t) {
        if (usuarioActual == null) return -1;
        int idNotif = facade.crearAlertaSeguridad(usuarioActual, t);
        return idNotif;
    }

    /**
     * PASO 1 de confirmación de alerta: envía el código OTP para
     * que el usuario confirme la transferencia de alto monto.
     * Retorna null si fue exitoso, o mensaje de error.
     */
    public String enviarCodigoAlerta(Notificacion alerta,
                                     InicioSesion.CanalVerificacion canal) {
        if (usuarioActual == null)
            return "Sesión inválida.";
        if (alerta == null || alerta.getIdTransferencia() == null)
            return "No hay alerta pendiente de confirmación.";

        this.alertaPendiente = alerta;

        codigoGenerado    = SeguridadUtil.generarCodigo6Digitos();
        String codigoHash = SeguridadUtil.hashSHA256(codigoGenerado);

        // Reutiliza TransferenciaDAO para crear la sesión OTP de la transferencia
        TransferenciaDAO transDAO = new TransferenciaDAO();
        idSesionAlerta = transDAO.crearSesionTransferencia(
                usuarioActual.getIdUsuario(),
                canal.getIdTipo(),
                alerta.getIdTransferencia(),
                codigoHash);

        if (idSesionAlerta < 0)
            return "Error al generar el código. Intente nuevamente.";

        String destinatario = (canal == InicioSesion.CanalVerificacion.SMS)
                ? usuarioActual.getTelefono()
                : usuarioActual.getEmail();

        // Reutiliza NotificacionFactory del Módulo 1 para enviar el código
        NotificacionObserver notificador = NotificacionFactory.crear(canal);
        notificador.enviarCodigo(destinatario, codigoGenerado,
                usuarioActual.getPrimerNombre());

        return null;
    }

    /**
     * PASO 2 de confirmación de alerta: verifica el código OTP y
     * procesa la transferencia pendiente usando TransferenciaFacade.
     * Retorna null si fue exitoso, o mensaje de error.
     */
    public String confirmarAlerta(String codigoIngresado,
                                  InicioSesion.CanalVerificacion canal) {
        if (alertaPendiente == null)
            return "No hay alerta pendiente de confirmación.";
        if (codigoIngresado == null || codigoIngresado.trim().isEmpty())
            return "Ingrese el código de verificación.";

        String codigoHash = SeguridadUtil.hashSHA256(codigoIngresado.trim());

        // Procesar la transferencia usando el SP de procesamiento completo
        TransferenciaDAO transDAO = new TransferenciaDAO();
        boolean exitosa = transDAO.procesarTransferencia(
                alertaPendiente.getIdTransferencia(), codigoHash, idSesionAlerta);

        if (!exitosa) {
            return "Código incorrecto o expirado. La transferencia sigue pendiente.";
        }

        // Notificar al usuario que la transferencia fue confirmada y procesada
        Transferencia t = transDAO.obtenerPorId(alertaPendiente.getIdTransferencia());
        if (t != null) {
            facade.enviarAlertaConfirmada(usuarioActual, alertaPendiente);
            facade.notificarTransferenciaEfectuada(usuarioActual, t);
        }

        limpiarEstadoAlerta();
        return null;
    }

    /**
     * Cancela una transferencia pendiente de confirmación de alerta.
     * Retorna null si fue exitoso, o mensaje de error.
     */
    public String cancelarAlerta(int idTransferencia) {
        boolean ok = facade.cancelarTransferenciaPendiente(
                idTransferencia, usuarioActual.getIdUsuario());
        if (!ok) return "No se pudo cancelar la transferencia.";
        limpiarEstadoAlerta();
        return null;
    }

    // ============================================================
    // CONSULTAS Y GESTIÓN
    // ============================================================

    public List<Notificacion> obtenerNotificaciones() {
        if (usuarioActual == null) return List.of();
        return facade.obtenerNotificaciones(usuarioActual.getIdUsuario());
    }

    public int contarNoLeidas() {
        if (usuarioActual == null) return 0;
        return facade.contarNoLeidas(usuarioActual.getIdUsuario());
    }

    public void marcarLeida(int idNotificacion) {
        facade.marcarLeida(idNotificacion);
    }

    public void marcarTodasLeidas() {
        if (usuarioActual != null)
            facade.marcarTodasLeidas(usuarioActual.getIdUsuario());
    }

    // ============================================================
    // GETTERS DE ESTADO
    // ============================================================

    public String  getCodigoGeneradoSimulacion() { return codigoGenerado; }
    public Notificacion getAlertaPendiente()      { return alertaPendiente; }

    private void limpiarEstadoAlerta() {
        alertaPendiente = null;
        idSesionAlerta  = -1;
        codigoGenerado  = null;
    }
}
