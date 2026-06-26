package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ============================================================
 * PATRÓN: STATE
 * ============================================================
 * Representa una transferencia de fondos. Su ciclo de vida
 * pasa por distintos estados:
 *
 *   PENDIENTE   → Transferencia registrada, esperando OTP
 *   PROCESANDO  → Código verificado, descuento en curso
 *   EXITOSA     → Fondos transferidos correctamente
 *   RECHAZADA   → Fondos insuficientes o código incorrecto
 *
 * Clase: Transferencia
 * Módulo: Transferencias — QoriBank
 * ============================================================
 */
public class Transferencia implements Cloneable {

    // --------------------------------------------------------
    // PATRÓN STATE: estados del ciclo de vida
    // --------------------------------------------------------
    public enum EstadoTransferencia {
        PENDIENTE,
        PROCESANDO,
        EXITOSA,
        RECHAZADA
    }

    // --------------------------------------------------------
    // Tipos de transferencia
    // --------------------------------------------------------
    public enum TipoTransferencia {
        ENTRE_CUENTAS(1),
        INTERNA(2),
        INTERBANCARIA(3);

        private final int idTipo;
        TipoTransferencia(int idTipo) { this.idTipo = idTipo; }
        public int getIdTipo() { return idTipo; }
    }

    // --- Atributos mapeados a dbo.Transferencia ---
    private int                  idTransferencia;
    private int                  idUsuario;
    private TipoTransferencia    tipo;
    private int                  idCuentaOrigen;
    private String               numeroTarjetaOrigenEnmascarado;
    private String               numeroTarjetaDestinoHash;
    private String               numeroTarjetaDestinoEnmascarado;
    private String               entidadDestino;
    private BigDecimal           monto;
    private String               descripcion;
    private EstadoTransferencia  estado;
    private LocalDateTime        creadoEn;
    private LocalDateTime        procesadoEn;

    // --------------------------------------------------------
    // Constructor vacío
    // --------------------------------------------------------
    public Transferencia() {
        this.estado   = EstadoTransferencia.PENDIENTE;
        this.creadoEn = LocalDateTime.now();
    }

    // --------------------------------------------------------
    // Constructor de registro inicial
    // --------------------------------------------------------
    public Transferencia(int idUsuario, TipoTransferencia tipo,
                         int idCuentaOrigen, String origenEnmascarado,
                         String destinoHash, String destinoEnmascarado,
                         String entidadDestino, BigDecimal monto,
                         String descripcion) {
        this.idUsuario                      = idUsuario;
        this.tipo                           = tipo;
        this.idCuentaOrigen                 = idCuentaOrigen;
        this.numeroTarjetaOrigenEnmascarado = origenEnmascarado;
        this.numeroTarjetaDestinoHash       = destinoHash;
        this.numeroTarjetaDestinoEnmascarado = destinoEnmascarado;
        this.entidadDestino                 = entidadDestino;
        this.monto                          = monto;
        this.descripcion                    = descripcion;
        this.estado                         = EstadoTransferencia.PENDIENTE;
        this.creadoEn                       = LocalDateTime.now();
    }

    // --------------------------------------------------------
    // PATRÓN STATE: transiciones
    // --------------------------------------------------------
    public void iniciarProcesamiento() {
        this.estado = EstadoTransferencia.PROCESANDO;
    }

    public void marcarExitosa() {
        this.estado      = EstadoTransferencia.EXITOSA;
        this.procesadoEn = LocalDateTime.now();
    }

    public void rechazar() {
        this.estado      = EstadoTransferencia.RECHAZADA;
        this.procesadoEn = LocalDateTime.now();
    }

    public boolean estaExitosa() {
        return estado == EstadoTransferencia.EXITOSA;
    }

    // --------------------------------------------------------
    // PATRÓN PROTOTYPE: clonar una transferencia como plantilla
    // (útil para repetir una transferencia frecuente)
    // --------------------------------------------------------
    @Override
    public Transferencia clone() {
        try {
            Transferencia clon = (Transferencia) super.clone();
            clon.idTransferencia = 0;
            clon.estado          = EstadoTransferencia.PENDIENTE;
            clon.creadoEn        = LocalDateTime.now();
            clon.procesadoEn     = null;
            return clon;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("[Transferencia] Error al clonar: " + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // Getters y Setters
    // --------------------------------------------------------
    public int getIdTransferencia()                         { return idTransferencia; }
    public void setIdTransferencia(int id)                  { this.idTransferencia = id; }

    public int getIdUsuario()                               { return idUsuario; }
    public void setIdUsuario(int id)                        { this.idUsuario = id; }

    public TipoTransferencia getTipo()                      { return tipo; }
    public void setTipo(TipoTransferencia tipo)             { this.tipo = tipo; }

    public int getIdCuentaOrigen()                          { return idCuentaOrigen; }
    public void setIdCuentaOrigen(int id)                   { this.idCuentaOrigen = id; }

    public String getNumeroTarjetaOrigenEnmascarado()       { return numeroTarjetaOrigenEnmascarado; }
    public void setNumeroTarjetaOrigenEnmascarado(String v) { this.numeroTarjetaOrigenEnmascarado = v; }

    public String getNumeroTarjetaDestinoHash()             { return numeroTarjetaDestinoHash; }
    public void setNumeroTarjetaDestinoHash(String v)       { this.numeroTarjetaDestinoHash = v; }

    public String getNumeroTarjetaDestinoEnmascarado()      { return numeroTarjetaDestinoEnmascarado; }
    public void setNumeroTarjetaDestinoEnmascarado(String v){ this.numeroTarjetaDestinoEnmascarado = v; }

    public String getEntidadDestino()                       { return entidadDestino; }
    public void setEntidadDestino(String v)                 { this.entidadDestino = v; }

    public BigDecimal getMonto()                            { return monto; }
    public void setMonto(BigDecimal monto)                  { this.monto = monto; }

    public String getDescripcion()                          { return descripcion; }
    public void setDescripcion(String v)                    { this.descripcion = v; }

    public EstadoTransferencia getEstado()                  { return estado; }
    public void setEstado(EstadoTransferencia estado)       { this.estado = estado; }

    public LocalDateTime getCreadoEn()                      { return creadoEn; }
    public void setCreadoEn(LocalDateTime v)                { this.creadoEn = v; }

    public LocalDateTime getProcesadoEn()                   { return procesadoEn; }
    public void setProcesadoEn(LocalDateTime v)             { this.procesadoEn = v; }

    @Override
    public String toString() {
        return "Transferencia{id=" + idTransferencia
               + ", tipo=" + tipo
               + ", monto=" + monto
               + ", estado=" + estado + "}";
    }
}
