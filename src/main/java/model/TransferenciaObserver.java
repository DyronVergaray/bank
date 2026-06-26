package model;

/**
 * ============================================================
 * PATRÓN: OBSERVER
 * ============================================================
 * Contrato para los observadores que reaccionan a eventos del
 * ciclo de vida de una transferencia:
 *   - Transferencia exitosa → notificar al usuario
 *   - Transferencia rechazada → notificar el motivo
 *
 * Interfaz: TransferenciaObserver
 * Módulo: Transferencias — QoriBank
 * ============================================================
 */
public interface TransferenciaObserver {

    /**
     * Invocado cuando una transferencia se completó exitosamente.
     *
     * @param transferencia  La transferencia completada
     * @param nombreUsuario  Nombre del remitente
     */
    void onTransferenciaExitosa(Transferencia transferencia, String nombreUsuario);

    /**
     * Invocado cuando una transferencia fue rechazada.
     *
     * @param transferencia  La transferencia rechazada
     * @param nombreUsuario  Nombre del remitente
     * @param motivo         Razón del rechazo
     */
    void onTransferenciaRechazada(Transferencia transferencia, String nombreUsuario, String motivo);
}


/**
 * ============================================================
 * PATRÓN: OBSERVER (implementación concreta)
 * ============================================================
 * Notifica al usuario (por SMS o Correo) el resultado de su
 * transferencia, reutilizando NotificacionFactory del Módulo 1.
 *
 * Clase: NotificacionTransferenciaObserver
 * ============================================================
 */
class NotificacionTransferenciaObserver implements TransferenciaObserver {

    private final InicioSesion.CanalVerificacion canal;
    private final String destinatario;

    public NotificacionTransferenciaObserver(InicioSesion.CanalVerificacion canal,
                                              String destinatario) {
        this.canal        = canal;
        this.destinatario = destinatario;
    }

    @Override
    public void onTransferenciaExitosa(Transferencia t, String nombreUsuario) {
        // REUTILIZA Factory + Observer del Módulo 1
        NotificacionObserver notificador = NotificacionFactory.crear(canal);
        String msg = String.format(
                "Transferencia exitosa de S/ %.2f a %s (%s)",
                t.getMonto(), t.getNumeroTarjetaDestinoEnmascarado(), t.getEntidadDestino());
        notificador.enviarCodigo(destinatario, msg, nombreUsuario);
    }

    @Override
    public void onTransferenciaRechazada(Transferencia t, String nombreUsuario, String motivo) {
        NotificacionObserver notificador = NotificacionFactory.crear(canal);
        String msg = String.format(
                "Transferencia de S/ %.2f RECHAZADA: %s",
                t.getMonto(), motivo);
        notificador.enviarCodigo(destinatario, msg, nombreUsuario);
    }
}
