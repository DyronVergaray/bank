package model;

import java.math.BigDecimal;

/**
 * ============================================================
 * PATRÓN: FACTORY
 * ============================================================
 * Centraliza la creación del objeto Transferencia correcto
 * según el tipo elegido por el usuario. El controlador no
 * necesita saber qué subclase instanciar.
 *
 * Clase: TransferenciaFactory
 * Módulo: Transferencias — QoriBank
 * ============================================================
 */
public class TransferenciaFactory {

    /**
     * Crea y retorna la subclase de Transferencia correspondiente
     * al tipo indicado.
     *
     * @param tipo              Tipo de transferencia elegido por el usuario
     * @param idUsuario         Id del usuario que realiza la transferencia
     * @param idCuentaOrigen    Id de la cuenta bancaria origen
     * @param origenEnmascarado Tarjeta origen enmascarada (para historial)
     * @param destinoHash       Hash SHA-256 del número de tarjeta destino
     * @param destinoEnmascarado Tarjeta destino enmascarada (para historial)
     * @param entidadDestino    Nombre del banco destino
     * @param monto             Monto a transferir
     * @param descripcion       Descripción/concepto opcional
     * @return                  Subclase concreta de Transferencia
     */
    public static Transferencia crear(Transferencia.TipoTransferencia tipo,
                                      int idUsuario, int idCuentaOrigen,
                                      String origenEnmascarado,
                                      String destinoHash, String destinoEnmascarado,
                                      String entidadDestino, BigDecimal monto,
                                      String descripcion) {
        if (tipo == null)
            throw new IllegalArgumentException("[TransferenciaFactory] tipo no puede ser null");

        switch (tipo) {
            case ENTRE_CUENTAS:
                return new TransferenciaEntreCuentas(idUsuario, idCuentaOrigen,
                        origenEnmascarado, destinoHash, destinoEnmascarado,
                        entidadDestino, monto, descripcion);
            case INTERNA:
                return new TransferenciaInterna(idUsuario, idCuentaOrigen,
                        origenEnmascarado, destinoHash, destinoEnmascarado,
                        entidadDestino, monto, descripcion);
            case INTERBANCARIA:
                return new TransferenciaInterbancaria(idUsuario, idCuentaOrigen,
                        origenEnmascarado, destinoHash, destinoEnmascarado,
                        entidadDestino, monto, descripcion);
            default:
                throw new IllegalArgumentException(
                        "[TransferenciaFactory] Tipo no soportado: " + tipo);
        }
    }
}
