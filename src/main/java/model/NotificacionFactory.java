package model;

/**
 * ============================================================
 * PATRÓN: OBSERVER (implementación concreta — SMS)
 * PATRÓN: FACTORY  (creado por NotificacionFactory)
 * ============================================================
 * Simula el envío de un código de verificación por SMS.
 * En producción, aquí se integraría un proveedor como Twilio.
 *
 * Clase: NotificacionSMS
 * ============================================================
 */
class NotificacionSMS implements NotificacionObserver {

    @Override
    public void enviarCodigo(String telefono, String codigo, String nombreUsuario) {
        // --- SIMULACIÓN DE ENVÍO POR SMS ---
        // En producción: llamada a API de Twilio / Amazon SNS / etc.
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║         QORI BANK — SMS              ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║ Para: " + telefono);
        System.out.println("║ Hola " + nombreUsuario + ",");
        System.out.println("║ Tu código de verificación es: " + codigo);
        System.out.println("║ Válido por 10 minutos.");
        System.out.println("║ No lo compartas con nadie.");
        System.out.println("╚══════════════════════════════════════╝");
    }
}


/**
 * ============================================================
 * PATRÓN: OBSERVER (implementación concreta — Correo)
 * PATRÓN: FACTORY  (creado por NotificacionFactory)
 * ============================================================
 * Simula el envío de un código de verificación por correo.
 * En producción, aquí se integraría JavaMail / SendGrid / etc.
 *
 * Clase: NotificacionCorreo
 * ============================================================
 */
class NotificacionCorreo implements NotificacionObserver {

    @Override
    public void enviarCodigo(String email, String codigo, String nombreUsuario) {
        // --- SIMULACIÓN DE ENVÍO POR CORREO ---
        // En producción: javax.mail con servidor SMTP / SendGrid API
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║      QORI BANK — CORREO              ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║ Para:    " + email);
        System.out.println("║ Asunto:  Código de verificación QoriBank");
        System.out.println("║ Cuerpo:");
        System.out.println("║   Estimado/a " + nombreUsuario + ",");
        System.out.println("║   Su código de acceso es: " + codigo);
        System.out.println("║   Válido por 10 minutos.");
        System.out.println("║   Si no solicitó este código, ignore este mensaje.");
        System.out.println("╚══════════════════════════════════════╝");
    }
}


/**
 * ============================================================
 * PATRÓN: FACTORY
 * ============================================================
 * Centraliza la creación de objetos NotificacionObserver según
 * el canal elegido por el usuario (SMS o CORREO).
 * El controlador no necesita saber qué clase concreta instanciar.
 *
 * Clase: NotificacionFactory
 * ============================================================
 */
public class NotificacionFactory {

    /**
     * Crea y retorna el observador de notificación correspondiente al canal.
     *
     * @param canal  Canal de verificación elegido
     * @return       Implementación concreta de NotificacionObserver
     */
    public static NotificacionObserver crear(InicioSesion.CanalVerificacion canal) {
        switch (canal) {
            case SMS:
                return new NotificacionSMS();
            case CORREO:
                return new NotificacionCorreo();
            default:
                throw new IllegalArgumentException(
                    "[NotificacionFactory] Canal no soportado: " + canal);
        }
    }
}
