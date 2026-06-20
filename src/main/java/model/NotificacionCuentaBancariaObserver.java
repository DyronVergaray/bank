package model;

/**
 * ============================================================
 * PATRÓN: OBSERVER (implementación concreta)
 * ============================================================
 * Reacciona a los eventos del ciclo de vida de una cuenta
 * bancaria notificando al usuario por el mismo canal (SMS o
 * Correo) que ya usó para verificar su identidad, reutilizando
 * NotificacionFactory + NotificacionObserver del Módulo 1.
 *
 * Clase: NotificacionCuentaBancariaObserver
 * Módulo: Vinculación de Cuentas Bancarias — QoriBank
 * ============================================================
 */
public class NotificacionCuentaBancariaObserver implements CuentaBancariaObserver {

    private final InicioSesion.CanalVerificacion canal;
    private final String destinatario;

    public NotificacionCuentaBancariaObserver(InicioSesion.CanalVerificacion canal, String destinatario) {
        this.canal        = canal;
        this.destinatario = destinatario;
    }

    @Override
    public void onCuentaVinculada(CuentaBancaria cuenta, String nombreUsuario) {
        // PATRÓN FACTORY + OBSERVER (reutilizado del Módulo 1)
        NotificacionObserver notificador = NotificacionFactory.crear(canal);
        String mensaje = "Tu cuenta " + cuenta.getNombreEntidad()
                + " terminada en " + ultimosDigitos(cuenta.getNumeroTarjetaEnmascarado())
                + " fue vinculada exitosamente";
        notificador.enviarCodigo(destinatario, mensaje, nombreUsuario);
    }

    @Override
    public void onErrorSincronizacion(CuentaBancaria cuenta, String nombreUsuario, String motivoError) {
        NotificacionObserver notificador = NotificacionFactory.crear(canal);
        String mensaje = "No se pudo vincular tu cuenta " + cuenta.getNombreEntidad()
                + ": " + motivoError;
        notificador.enviarCodigo(destinatario, mensaje, nombreUsuario);
    }

    private String ultimosDigitos(String enmascarado) {
        if (enmascarado == null || enmascarado.length() < 4) return "****";
        return enmascarado.substring(enmascarado.length() - 4);
    }
}
