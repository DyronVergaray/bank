package model;

/**
 * ============================================================
 * PATRÓN: OBSERVER
 * ============================================================
 * Define el contrato para todos los observadores de notificación.
 * Cada canal (SMS, Correo) implementa esta interfaz y reacciona
 * al evento de "código de verificación generado".
 *
 * Cuando el controlador genera un código, notifica a todos los
 * observadores registrados (en este módulo solo el canal elegido).
 *
 * Interfaz: NotificacionObserver
 * Módulo: Gestión de Usuarios y Autenticación — QoriBank
 * ============================================================
 */
public interface NotificacionObserver {

    /**
     * Método invocado cuando se debe enviar el código de verificación.
     *
     * @param destinatario  Email o número de teléfono del usuario
     * @param codigo        Código de 6 dígitos en texto plano
     * @param nombreUsuario Nombre del usuario para personalizar el mensaje
     */
    void enviarCodigo(String destinatario, String codigo, String nombreUsuario);
}
