package model;

/**
 * ============================================================
 * PATRÓN: SINGLETON
 * ============================================================
 * Centraliza la configuración del servidor/proveedor de
 * notificaciones. Garantiza una única instancia en toda la
 * aplicación (igual que ConexionBD).
 *
 * En producción almacenaría credenciales de Twilio (SMS),
 * SendGrid (Correo) y FCM (Push). Aquí simula los valores
 * para el entorno de desarrollo del proyecto.
 *
 * Clase: ConfiguracionNotificacion
 * Módulo: Notificaciones y Alertas — QoriBank
 * ============================================================
 */
public class ConfiguracionNotificacion {

    // --- SINGLETON: instancia única ---
    private static ConfiguracionNotificacion instancia;

    // Configuración SMS (simulada — en producción: Twilio API)
    private final String smsApiUrl   = "https://api.twilio.com/2010-04-01/Messages";
    private final String smsApiKey   = "qoribank_sms_demo_key";
    private final String smsRemitente = "QoriBank";

    // Configuración Correo (simulada — en producción: SendGrid/JavaMail)
    private final String correoSmtpHost = "smtp.sendgrid.net";
    private final int    correoSmtpPort = 587;
    private final String correoApiKey   = "qoribank_email_demo_key";
    private final String correoRemitente = "notificaciones@qoribank.pe";

    // Configuración Push (simulada — en producción: Firebase FCM)
    private final String pushFcmUrl = "https://fcm.googleapis.com/fcm/send";
    private final String pushApiKey = "qoribank_push_demo_key";

    // Umbral de alerta de seguridad (S/ 500)
    public static final java.math.BigDecimal UMBRAL_ALERTA =
            new java.math.BigDecimal("500.00");

    // --------------------------------------------------------
    // Constructor privado
    // --------------------------------------------------------
    private ConfiguracionNotificacion() {
        System.out.println("[ConfiguracionNotificacion] Instancia creada — modo simulación.");
    }

    // --------------------------------------------------------
    // SINGLETON: punto de acceso global (thread-safe)
    // --------------------------------------------------------
    public static ConfiguracionNotificacion getInstancia() {
        if (instancia == null) {
            synchronized (ConfiguracionNotificacion.class) {
                if (instancia == null) {
                    instancia = new ConfiguracionNotificacion();
                }
            }
        }
        return instancia;
    }

    // --------------------------------------------------------
    // Getters de configuración
    // --------------------------------------------------------
    public String getSmsApiUrl()       { return smsApiUrl; }
    public String getSmsApiKey()       { return smsApiKey; }
    public String getSmsRemitente()    { return smsRemitente; }

    public String getCorreoSmtpHost()  { return correoSmtpHost; }
    public int    getCorreoSmtpPort()  { return correoSmtpPort; }
    public String getCorreoApiKey()    { return correoApiKey; }
    public String getCorreoRemitente() { return correoRemitente; }

    public String getPushFcmUrl()      { return pushFcmUrl; }
    public String getPushApiKey()      { return pushApiKey; }
}
