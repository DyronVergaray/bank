package model;

import java.time.format.DateTimeFormatter;

/**
 * ============================================================
 * PATRÓN: FACADE
 * ============================================================
 * Encapsula toda la complejidad del proceso de notificación
 * detrás de métodos simples. El controlador llama a un solo
 * método sin necesidad de conocer el ADAPTER, el DAO, el
 * SINGLETON de configuración ni el formato del mensaje.
 *
 * Flujo interno que orquesta:
 *   1. Construir el mensaje con la plantilla (PROTOTYPE)
 *   2. Seleccionar el proveedor correcto (ADAPTER)
 *   3. Enviar la notificación
 *   4. Persistir en BD vía NotificacionDAO (STATE → ENVIADA/FALLIDA)
 *
 * Clase: NotificacionFacade
 * Módulo: Notificaciones y Alertas — QoriBank
 * ============================================================
 */
public class NotificacionFacade {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");

    private final NotificacionDAO dao;

    public NotificacionFacade() {
        this.dao = new NotificacionDAO();
    }

    // ============================================================
    // MÉTODOS DE ENVÍO (uno por tipo de evento)
    // ============================================================

    /**
     * Notifica al usuario que inició sesión correctamente.
     */
    public void notificarInicioSesion(Usuario usuario) {
        String canal = dao.obtenerCanalPreferido(usuario.getIdUsuario());
        String fecha = java.time.LocalDateTime.now().format(FMT);

        // PROTOTYPE: usar plantilla de sesión
        Notificacion n = Notificacion.plantillaSesion(
                usuario.getIdUsuario(), canal);
        n.setMensaje("Inicio de sesión detectado el " + fecha
                + ". Si no fuiste tú, contacta a soporte.");

        enviarYPersistir(n, usuario, "Inicio de Sesión — QoriBank");
    }

    /**
     * Notifica al remitente que su transferencia fue efectuada.
     */
    public void notificarTransferenciaEfectuada(Usuario usuario,
            Transferencia t) {
        String canal = dao.obtenerCanalPreferido(usuario.getIdUsuario());
        String fecha = (t.getProcesadoEn() != null)
                ? t.getProcesadoEn().format(FMT)
                : java.time.LocalDateTime.now().format(FMT);

        String msg = String.format(
                "Transferiste S/ %,.2f el %s desde tu tarjeta %s hacia %s (%s).",
                t.getMonto(), fecha,
                t.getNumeroTarjetaOrigenEnmascarado(),
                t.getNumeroTarjetaDestinoEnmascarado(),
                t.getEntidadDestino());

        // PROTOTYPE: plantilla de transferencia
        Notificacion n = Notificacion.plantillaTransferencia(
                usuario.getIdUsuario(), t.getIdTransferencia(), msg, canal);

        enviarYPersistir(n, usuario, "Transferencia efectuada — QoriBank");
    }

    /**
     * Notifica al destinatario que recibió una transferencia.
     * Solo aplica si la tarjeta destino pertenece a un usuario del sistema.
     */
    public void notificarTransferenciaRecibida(Usuario destinatario,
            Transferencia t) {
        String canal = dao.obtenerCanalPreferido(destinatario.getIdUsuario());
        String fecha = (t.getProcesadoEn() != null)
                ? t.getProcesadoEn().format(FMT)
                : java.time.LocalDateTime.now().format(FMT);

        String msg = String.format(
                "Recibiste S/ %,.2f el %s en tu tarjeta %s.",
                t.getMonto(), fecha,
                t.getNumeroTarjetaDestinoEnmascarado());

        Notificacion n = Notificacion.plantillaTransferencia(
                destinatario.getIdUsuario(), t.getIdTransferencia(), msg, canal);

        enviarYPersistir(n, destinatario, "Transferencia recibida — QoriBank");
    }

    /**
     * Crea una alerta de seguridad para transferencias >= S/ 500.
     * La notificación queda en estado PENDIENTE (no se envía hasta
     * que el usuario confirme con OTP desde NotificacionesView).
     */
    public int crearAlertaSeguridad(Usuario usuario, Transferencia t) {
        String canal = dao.obtenerCanalPreferido(usuario.getIdUsuario());
        String fecha = java.time.LocalDateTime.now().format(FMT);

        String msg = String.format(
                "ALERTA: Transferencia de S/ %,.2f el %s desde %s hacia %s (%s) "
                + "requiere tu confirmación adicional por superar S/ 500.00.",
                t.getMonto(), fecha,
                t.getNumeroTarjetaOrigenEnmascarado(),
                t.getNumeroTarjetaDestinoEnmascarado(),
                t.getEntidadDestino());

        // PROTOTYPE: plantilla de alerta
        Notificacion n = Notificacion.plantillaAlerta(
                usuario.getIdUsuario(), t.getIdTransferencia(), msg, canal);
        // Estado PENDIENTE: se persiste pero NO se envía por canal todavía
        n.setEstado(Notificacion.EstadoNotificacion.PENDIENTE);

        int idNotificacion = dao.crearNotificacion(n);
        n.setIdNotificacion(idNotificacion);

        System.out.println("[NotificacionFacade] Alerta de seguridad creada (id="
                + idNotificacion + ") — transferencia id=" + t.getIdTransferencia());
        return idNotificacion;
    }

    /**
     * Envía la alerta de seguridad por canal tras confirmación OTP.
     * Cambia el estado de la notificación de PENDIENTE a ENVIADA.
     */
    public void enviarAlertaConfirmada(Usuario usuario, Notificacion alerta) {
        String asunto = "Transferencia confirmada — QoriBank";
        ProveedorNotificacionAdapter proveedor = crearProveedor(alerta.getCanal());
        String destinatario = obtenerDestinatario(usuario, alerta.getCanal());

        boolean ok = proveedor.enviar(destinatario, asunto, alerta.getMensaje());
        alerta.marcarEnviada();
        if (!ok) alerta.marcarFallida();

        dao.actualizarEstadoNotificacion(alerta.getIdNotificacion(),
                alerta.getEstado().name());
        dao.marcarLeida(alerta.getIdNotificacion());
    }

    // ============================================================
    // MÉTODOS DE CONSULTA (delegados al DAO)
    // ============================================================

    public java.util.List<Notificacion> obtenerNotificaciones(int idUsuario) {
        return dao.obtenerPorUsuario(idUsuario);
    }

    public int contarNoLeidas(int idUsuario) {
        return dao.contarNoLeidas(idUsuario);
    }

    public void marcarLeida(int idNotificacion) {
        dao.marcarLeida(idNotificacion);
    }

    public void marcarTodasLeidas(int idUsuario) {
        dao.marcarTodasLeidas(idUsuario);
    }

    public String obtenerCanalPreferido(int idUsuario) {
        return dao.obtenerCanalPreferido(idUsuario);
    }

    public void guardarPreferencia(int idUsuario, String canal) {
        dao.guardarPreferencia(idUsuario, canal);
    }

    public boolean cancelarTransferenciaPendiente(int idTransferencia, int idUsuario) {
        return dao.cancelarTransferenciaPendiente(idTransferencia, idUsuario);
    }

    // ============================================================
    // HELPERS PRIVADOS
    // ============================================================

    /** Envía por canal y persiste el resultado en BD. */
    private void enviarYPersistir(Notificacion n, Usuario usuario, String asunto) {
        // ADAPTER: seleccionar proveedor según canal
        ProveedorNotificacionAdapter proveedor = crearProveedor(n.getCanal());
        String destinatario = obtenerDestinatario(usuario, n.getCanal());

        boolean ok = proveedor.enviar(destinatario, asunto, n.getMensaje());

        // STATE: actualizar estado según resultado
        if (ok) n.marcarEnviada(); else n.marcarFallida();

        // Persistir en BD
        int idNotif = dao.crearNotificacion(n);
        n.setIdNotificacion(idNotif);

        System.out.println("[NotificacionFacade] Notificación " + n.getEstado()
                + " vía " + proveedor.getNombreProveedor()
                + " (id=" + idNotif + ")");
    }

    /** ADAPTER FACTORY: crea el proveedor según el canal elegido. */
    private ProveedorNotificacionAdapter crearProveedor(String canal) {
        if (canal == null) return new ProveedorCorreoAdapter();
        switch (canal.toUpperCase()) {
            case "SMS":   return new ProveedorSMSAdapter();
            case "PUSH":  return new ProveedorPushAdapter();
            default:      return new ProveedorCorreoAdapter();
        }
    }

    /** Retorna el destinatario (email o teléfono) según el canal. */
    private String obtenerDestinatario(Usuario usuario, String canal) {
        if ("SMS".equalsIgnoreCase(canal)) return usuario.getTelefono();
        if ("PUSH".equalsIgnoreCase(canal)) return "device_token_" + usuario.getIdUsuario();
        return usuario.getEmail();
    }
}
