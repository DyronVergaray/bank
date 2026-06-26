package model;

import util.SeguridadUtil;

/**
 * ============================================================
 * PATRÓN: FACADE
 * ============================================================
 * Encapsula el flujo completo y complejo de procesamiento de
 * una transferencia detrás de una interfaz simple. El
 * TransferenciaController solo llama a un método de esta
 * fachada en lugar de coordinar múltiples DAOs y validaciones.
 *
 * Flujo interno que orquesta:
 *   1. Verificar el código OTP
 *   2. Llamar a sp_ProcesarTransferencia (valida fondos,
 *      descuenta saldo origen, acredita destino si existe)
 *   3. Actualizar el estado del objeto Transferencia en memoria
 *      (PATRÓN STATE)
 *   4. Notificar al usuario el resultado (PATRÓN OBSERVER)
 *
 * Clase: TransferenciaFacade
 * Módulo: Transferencias — QoriBank
 * ============================================================
 */
public class TransferenciaFacade {

    private final TransferenciaDAO dao;

    public TransferenciaFacade() {
        this.dao = new TransferenciaDAO();
    }

    /**
     * Procesa la transferencia completa de principio a fin.
     *
     * @param transferencia     Objeto de transferencia en estado PENDIENTE
     * @param codigoIngresado   Código OTP ingresado por el usuario (texto plano)
     * @param idSesion          Id de la sesión OTP creada para esta transferencia
     * @param canal             Canal de notificación (SMS o Correo)
     * @param destinatario      Email o teléfono del usuario para notificar
     * @param nombreUsuario     Nombre del usuario (para el mensaje)
     * @return                  null si fue exitosa, o mensaje de error
     */
    public String procesarTransferencia(Transferencia transferencia,
                                        String codigoIngresado, int idSesion,
                                        InicioSesion.CanalVerificacion canal,
                                        String destinatario, String nombreUsuario) {

        if (codigoIngresado == null || codigoIngresado.trim().isEmpty())
            return "Ingrese el código de verificación.";

        String codigoHash = SeguridadUtil.hashSHA256(codigoIngresado.trim());

        // PATRÓN OBSERVER: crear el notificador
        TransferenciaObserver observador =
                new NotificacionTransferenciaObserver(canal, destinatario);

        // Delegar el procesamiento completo al SP (incluye verificación OTP,
        // validación de fondos, débito y crédito en una transacción SQL)
        boolean exitosa = dao.procesarTransferencia(
                transferencia.getIdTransferencia(), codigoHash, idSesion);

        if (exitosa) {
            // PATRÓN STATE: transición a EXITOSA
            transferencia.marcarExitosa();
            // PATRÓN OBSERVER: notificar éxito
            observador.onTransferenciaExitosa(transferencia, nombreUsuario);
            return null;
        } else {
            // PATRÓN STATE: transición a RECHAZADA
            transferencia.rechazar();
            String motivo = "Código incorrecto, expirado o fondos insuficientes.";
            // PATRÓN OBSERVER: notificar rechazo
            observador.onTransferenciaRechazada(transferencia, nombreUsuario, motivo);
            return motivo;
        }
    }
}
