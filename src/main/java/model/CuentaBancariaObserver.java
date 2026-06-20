package model;

/**
 * ============================================================
 * PATRÓN: OBSERVER
 * ============================================================
 * Define el contrato para los observadores que reaccionan a
 * eventos del ciclo de vida de una cuenta bancaria: cuando
 * queda vinculada exitosamente o cuando ocurre un error de
 * sincronización/validación.
 *
 * Esto reutiliza la misma idea que NotificacionObserver (Módulo 1),
 * pero con un contrato propio porque el evento aquí no es "enviar
 * un código", sino "informar el resultado de una operación sobre
 * la cuenta bancaria" — útil tanto para notificar al usuario por
 * SMS/Correo como, a futuro, para refrescar la UI en tiempo real.
 *
 * Interfaz: CuentaBancariaObserver
 * Módulo: Vinculación de Cuentas Bancarias — QoriBank
 * ============================================================
 */
public interface CuentaBancariaObserver {

    /**
     * Invocado cuando una cuenta bancaria queda vinculada
     * exitosamente (código OTP verificado correctamente).
     *
     * @param cuenta         La cuenta bancaria recién vinculada
     * @param nombreUsuario  Nombre del usuario dueño de la cuenta
     */
    void onCuentaVinculada(CuentaBancaria cuenta, String nombreUsuario);

    /**
     * Invocado cuando ocurre un error en la validación o
     * sincronización de una cuenta bancaria (ej. código OTP
     * incorrecto, o el banco rechaza la tarjeta).
     *
     * @param cuenta          La cuenta bancaria afectada
     * @param nombreUsuario   Nombre del usuario dueño de la cuenta
     * @param motivoError     Descripción breve del error
     */
    void onErrorSincronizacion(CuentaBancaria cuenta, String nombreUsuario, String motivoError);
}
