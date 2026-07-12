package model;

/**
 * ============================================================
 * PATRÓN: ADAPTER
 * ============================================================
 * Normaliza las diferencias entre proveedores externos de
 * notificaciones (Twilio/SMS, SendGrid/Correo, FCM/Push)
 * detrás de una interfaz común. El resto del sistema solo
 * conoce esta interfaz y no depende de ningún proveedor concreto.
 *
 * Interfaz: ProveedorNotificacionAdapter
 * Módulo: Notificaciones y Alertas — QoriBank
 * ============================================================
 */
public interface ProveedorNotificacionAdapter {

    /**
     * Envía una notificación al destinatario.
     *
     * @param destinatario  Email, teléfono o token de dispositivo
     * @param asunto        Asunto o título de la notificación
     * @param cuerpo        Mensaje completo
     * @return              true si el envío fue exitoso
     */
    boolean enviar(String destinatario, String asunto, String cuerpo);

    /** Nombre del proveedor (para logging y trazabilidad). */
    String getNombreProveedor();
}


// ============================================================
// ADAPTER — SMS (simulado: Twilio)
// ============================================================
class ProveedorSMSAdapter implements ProveedorNotificacionAdapter {

    private final ConfiguracionNotificacion config;

    ProveedorSMSAdapter() {
        // SINGLETON: obtiene la configuración centralizada
        this.config = ConfiguracionNotificacion.getInstancia();
    }

    @Override
    public boolean enviar(String telefono, String asunto, String cuerpo) {
        // --- SIMULACIÓN SMS (en producción: Twilio REST API) ---
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║     QORI BANK — NOTIFICACIÓN SMS         ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║ De:      " + config.getSmsRemitente());
        System.out.println("║ Para:    " + telefono);
        System.out.println("║ Asunto:  " + asunto);
        System.out.println("║ Mensaje: " + cuerpo);
        System.out.println("║ API URL: " + config.getSmsApiUrl());
        System.out.println("╚══════════════════════════════════════════╝");
        return true;
    }

    @Override
    public String getNombreProveedor() { return "SMS (Twilio - Simulado)"; }
}


// ============================================================
// ADAPTER — Correo (simulado: SendGrid)
// ============================================================
class ProveedorCorreoAdapter implements ProveedorNotificacionAdapter {

    private final ConfiguracionNotificacion config;

    ProveedorCorreoAdapter() {
        this.config = ConfiguracionNotificacion.getInstancia();
    }

    @Override
    public boolean enviar(String email, String asunto, String cuerpo) {
        // --- SIMULACIÓN CORREO (en producción: SendGrid / JavaMail SMTP) ---
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║    QORI BANK — NOTIFICACIÓN CORREO       ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║ De:      " + config.getCorreoRemitente());
        System.out.println("║ Para:    " + email);
        System.out.println("║ Asunto:  " + asunto);
        System.out.println("║ Cuerpo:");
        System.out.println("║   " + cuerpo);
        System.out.println("║ SMTP:    " + config.getCorreoSmtpHost()
                           + ":" + config.getCorreoSmtpPort());
        System.out.println("╚══════════════════════════════════════════╝");
        return true;
    }

    @Override
    public String getNombreProveedor() { return "Correo (SendGrid - Simulado)"; }
}


// ============================================================
// ADAPTER — Push Móvil (simulado: FCM por consola)
// ============================================================
class ProveedorPushAdapter implements ProveedorNotificacionAdapter {

    private final ConfiguracionNotificacion config;

    ProveedorPushAdapter() {
        this.config = ConfiguracionNotificacion.getInstancia();
    }

    @Override
    public boolean enviar(String tokenDispositivo, String asunto, String cuerpo) {
        // --- SIMULACIÓN PUSH MÓVIL (en producción: Firebase FCM REST API) ---
        // En producción: POST a fcm.googleapis.com con el token del dispositivo.
        // Aquí se muestra por consola como fue acordado (Opción B).
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   QORI BANK — NOTIFICACIÓN PUSH MÓVIL   ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║ Proveedor: Firebase FCM (Simulado)       ║");
        System.out.println("║ Token:     " + (tokenDispositivo != null
                           ? tokenDispositivo.substring(0, Math.min(20, tokenDispositivo.length())) + "..."
                           : "N/A"));
        System.out.println("║ Titulo:    " + asunto);
        System.out.println("║ Cuerpo:    " + cuerpo);
        System.out.println("║ FCM URL:   " + config.getPushFcmUrl());
        System.out.println("╚══════════════════════════════════════════╝");
        return true;
    }

    @Override
    public String getNombreProveedor() { return "Push Móvil (FCM - Simulado)"; }
}
