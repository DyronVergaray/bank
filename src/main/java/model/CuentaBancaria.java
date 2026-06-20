package model;

import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * ============================================================
 * PATRÓN: STATE
 * ============================================================
 * Representa una cuenta bancaria (tarjeta) vinculada por un
 * cliente. Mapea la tabla dbo.Cuenta_Bancaria.
 *
 * El ciclo de vida de una cuenta bancaria pasa por distintos
 * estados durante el flujo de vinculación:
 *
 *   PENDIENTE  → Tarjeta registrada, esperando verificación OTP
 *   VINCULADA  → Código confirmado, la cuenta queda activa
 *   ERROR      → El código fue incorrecto o el proceso falló
 *
 * Clase: CuentaBancaria
 * Módulo: Vinculación de Cuentas Bancarias — QoriBank
 * ============================================================
 */
public class CuentaBancaria {

    // --------------------------------------------------------
    // PATRÓN STATE: enumeración de estados posibles
    // --------------------------------------------------------
    public enum EstadoCuenta {
        PENDIENTE,
        VINCULADA,
        ERROR
    }

    // --- Atributos mapeados a dbo.Cuenta_Bancaria ---
    private int            idCuentaBancaria;
    private int             idUsuario;
    private int             idTipoCuenta;
    private String          nombreEntidad;          // viene del JOIN con Tipo_Cuenta_Banco
    private String          numeroTarjetaHash;       // nunca se expone en la vista
    private String          numeroTarjetaEnmascarado; // ej: **** **** **** 4321
    private String          fechaVencimiento;         // formato MM/AAAA
    private String          cvvHash;                  // nunca se expone en la vista
    private EstadoCuenta     estado;
    private BigDecimal       saldo;
    private LocalDateTime    creadoEn;

    // --------------------------------------------------------
    // Constructor vacío
    // --------------------------------------------------------
    public CuentaBancaria() {
        this.estado = EstadoCuenta.PENDIENTE;
        this.saldo  = BigDecimal.ZERO;
    }

    // --------------------------------------------------------
    // Constructor para registro inicial (antes de persistir)
    // --------------------------------------------------------
    public CuentaBancaria(int idUsuario, int idTipoCuenta,
                          String numeroTarjetaHash, String numeroTarjetaEnmascarado,
                          String fechaVencimiento, String cvvHash) {
        this.idUsuario                = idUsuario;
        this.idTipoCuenta             = idTipoCuenta;
        this.numeroTarjetaHash         = numeroTarjetaHash;
        this.numeroTarjetaEnmascarado  = numeroTarjetaEnmascarado;
        this.fechaVencimiento          = fechaVencimiento;
        this.cvvHash                   = cvvHash;
        this.estado                    = EstadoCuenta.PENDIENTE;
        this.saldo                     = BigDecimal.ZERO;
    }

    // --------------------------------------------------------
    // PATRÓN STATE: transiciones de estado
    // --------------------------------------------------------

    /** Avanza al estado VINCULADA cuando el código OTP es correcto */
    public void vincular() {
        this.estado = EstadoCuenta.VINCULADA;
    }

    /** Transiciona al estado ERROR (código incorrecto / fallo de vinculación) */
    public void marcarError() {
        this.estado = EstadoCuenta.ERROR;
    }

    /** Verifica si la cuenta ya está activa y operativa */
    public boolean estaVinculada() {
        return estado == EstadoCuenta.VINCULADA;
    }

    // --------------------------------------------------------
    // Getters y Setters
    // --------------------------------------------------------
    public int getIdCuentaBancaria()                  { return idCuentaBancaria; }
    public void setIdCuentaBancaria(int id)            { this.idCuentaBancaria = id; }

    public int getIdUsuario()                          { return idUsuario; }
    public void setIdUsuario(int idUsuario)            { this.idUsuario = idUsuario; }

    public int getIdTipoCuenta()                       { return idTipoCuenta; }
    public void setIdTipoCuenta(int idTipoCuenta)      { this.idTipoCuenta = idTipoCuenta; }

    public String getNombreEntidad()                   { return nombreEntidad; }
    public void setNombreEntidad(String nombreEntidad) { this.nombreEntidad = nombreEntidad; }

    public String getNumeroTarjetaHash()                { return numeroTarjetaHash; }
    public void setNumeroTarjetaHash(String hash)       { this.numeroTarjetaHash = hash; }

    public String getNumeroTarjetaEnmascarado()         { return numeroTarjetaEnmascarado; }
    public void setNumeroTarjetaEnmascarado(String v)   { this.numeroTarjetaEnmascarado = v; }

    public String getFechaVencimiento()                 { return fechaVencimiento; }
    public void setFechaVencimiento(String v)           { this.fechaVencimiento = v; }

    public String getCvvHash()                          { return cvvHash; }
    public void setCvvHash(String cvvHash)              { this.cvvHash = cvvHash; }

    public EstadoCuenta getEstado()                     { return estado; }
    public void setEstado(EstadoCuenta estado)          { this.estado = estado; }

    public BigDecimal getSaldo()                        { return saldo; }
    public void setSaldo(BigDecimal saldo)              { this.saldo = saldo; }

    public LocalDateTime getCreadoEn()                  { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn)     { this.creadoEn = creadoEn; }

    @Override
    public String toString() {
        return "CuentaBancaria{id=" + idCuentaBancaria
               + ", entidad='" + nombreEntidad + "'"
               + ", tarjeta='" + numeroTarjetaEnmascarado + "'"
               + ", estado=" + estado + "}";
    }
}
