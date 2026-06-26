package model;

import java.math.BigDecimal;

/**
 * ============================================================
 * PATRÓN: FACTORY (productos concretos)
 * ============================================================
 * Subclases de Transferencia con lógica específica por tipo.
 * Creadas exclusivamente a través de TransferenciaFactory.
 *
 * TransferenciaInterna   → misma entidad bancaria que el origen
 * TransferenciaInterbancaria → entidad distinta al origen
 *
 * La distinción permite, a futuro, aplicar comisiones distintas
 * o lógica de validación diferente según el tipo.
 * ============================================================
 */

// ============================================================
// INTERNA: misma entidad bancaria
// ============================================================
class TransferenciaInterna extends Transferencia {

    public TransferenciaInterna(int idUsuario, int idCuentaOrigen,
                                String origenEnmascarado,
                                String destinoHash, String destinoEnmascarado,
                                String entidadDestino, BigDecimal monto,
                                String descripcion) {
        super(idUsuario, TipoTransferencia.INTERNA,
              idCuentaOrigen, origenEnmascarado,
              destinoHash, destinoEnmascarado,
              entidadDestino, monto, descripcion);
    }

    /**
     * Regla de negocio: las transferencias internas no tienen
     * comisión adicional (en este proyecto, comisión = 0).
     */
    public BigDecimal calcularComision() {
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "[INTERNA] " + super.toString();
    }
}


// ============================================================
// INTERBANCARIA: entidad distinta al origen
// ============================================================
class TransferenciaInterbancaria extends Transferencia {

    /** Porcentaje de comisión simulado para transferencias interbancarias */
    private static final BigDecimal COMISION_PCT = new BigDecimal("0.005"); // 0.5%

    public TransferenciaInterbancaria(int idUsuario, int idCuentaOrigen,
                                      String origenEnmascarado,
                                      String destinoHash, String destinoEnmascarado,
                                      String entidadDestino, BigDecimal monto,
                                      String descripcion) {
        super(idUsuario, TipoTransferencia.INTERBANCARIA,
              idCuentaOrigen, origenEnmascarado,
              destinoHash, destinoEnmascarado,
              entidadDestino, monto, descripcion);
    }

    /**
     * Regla de negocio: las transferencias interbancarias tienen
     * una comisión del 0.5% del monto (simulada).
     */
    public BigDecimal calcularComision() {
        return getMonto().multiply(COMISION_PCT).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "[INTERBANCARIA] " + super.toString();
    }
}


// ============================================================
// ENTRE_CUENTAS: entre dos tarjetas propias del mismo usuario
// ============================================================
class TransferenciaEntreCuentas extends Transferencia {

    public TransferenciaEntreCuentas(int idUsuario, int idCuentaOrigen,
                                     String origenEnmascarado,
                                     String destinoHash, String destinoEnmascarado,
                                     String entidadDestino, BigDecimal monto,
                                     String descripcion) {
        super(idUsuario, TipoTransferencia.ENTRE_CUENTAS,
              idCuentaOrigen, origenEnmascarado,
              destinoHash, destinoEnmascarado,
              entidadDestino, monto, descripcion);
    }

    public BigDecimal calcularComision() {
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "[ENTRE_CUENTAS] " + super.toString();
    }
}
