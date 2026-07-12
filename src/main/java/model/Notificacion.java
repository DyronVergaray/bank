package model;

import java.time.LocalDateTime;

/**
 * ============================================================
 * PATRÓN: STATE + PROTOTYPE
 * ============================================================
 * Representa una notificación generada para un usuario.
 * Estados: PENDIENTE → ENVIADA o FALLIDA (STATE).
 * Implementa Cloneable para clonar plantillas frecuentes
 * (ej. plantilla de alerta, de inicio de sesión) sin
 * reconstruir el objeto desde cero (PROTOTYPE).
 *
 * Clase: Notificacion
 * Módulo: Notificaciones y Alertas — QoriBank
 * ============================================================
 */
public class Notificacion implements Cloneable {

    // --------------------------------------------------------
    // PATRÓN STATE: estados del ciclo de vida
    // --------------------------------------------------------
    public enum EstadoNotificacion {
        PENDIENTE,
        ENVIADA,
        FALLIDA
    }

    // --------------------------------------------------------
    // Tipos de notificación
    // --------------------------------------------------------
    public enum TipoNotificacion {
        SESION(1),
        TRANSFERENCIA(2),
        ALERTA(3);

        private final int idTipo;
        TipoNotificacion(int id) { this.idTipo = id; }
        public int getIdTipo() { return idTipo; }
    }

    // --- Atributos mapeados a dbo.Notificacion ---
    private int                  idNotificacion;
    private int                  idUsuario;
    private TipoNotificacion     tipo;
    private Integer              idTransferencia; // nullable
    private String               mensaje;
    private String               canal;           // SMS, CORREO, PUSH
    private EstadoNotificacion   estado;
    private boolean              leida;
    private LocalDateTime        fechaHora;

    // --------------------------------------------------------
    // Constructor vacío
    // --------------------------------------------------------
    public Notificacion() {
        this.estado    = EstadoNotificacion.PENDIENTE;
        this.leida     = false;
        this.fechaHora = LocalDateTime.now();
    }

    // --------------------------------------------------------
    // Constructor completo
    // --------------------------------------------------------
    public Notificacion(int idUsuario, TipoNotificacion tipo,
                        Integer idTransferencia, String mensaje,
                        String canal) {
        this.idUsuario       = idUsuario;
        this.tipo            = tipo;
        this.idTransferencia = idTransferencia;
        this.mensaje         = mensaje;
        this.canal           = canal;
        this.estado          = EstadoNotificacion.PENDIENTE;
        this.leida           = false;
        this.fechaHora       = LocalDateTime.now();
    }

    // --------------------------------------------------------
    // PATRÓN STATE: transiciones
    // --------------------------------------------------------
    public void marcarEnviada() { this.estado = EstadoNotificacion.ENVIADA; }
    public void marcarFallida() { this.estado = EstadoNotificacion.FALLIDA; }
    public boolean estaEnviada() { return estado == EstadoNotificacion.ENVIADA; }

    // --------------------------------------------------------
    // PATRÓN PROTOTYPE: plantillas frecuentes
    // --------------------------------------------------------

    /** Plantilla para notificación de inicio de sesión */
    public static Notificacion plantillaSesion(int idUsuario, String canal) {
        return new Notificacion(idUsuario, TipoNotificacion.SESION, null,
                "Inicio de sesión detectado", canal);
    }

    /** Plantilla para notificación de transferencia */
    public static Notificacion plantillaTransferencia(int idUsuario,
            int idTransferencia, String mensaje, String canal) {
        return new Notificacion(idUsuario, TipoNotificacion.TRANSFERENCIA,
                idTransferencia, mensaje, canal);
    }

    /** Plantilla para alerta de seguridad */
    public static Notificacion plantillaAlerta(int idUsuario,
            int idTransferencia, String mensaje, String canal) {
        return new Notificacion(idUsuario, TipoNotificacion.ALERTA,
                idTransferencia, mensaje, canal);
    }

    /** PROTOTYPE: clonar una notificación como base para una nueva */
    @Override
    public Notificacion clone() {
        try {
            Notificacion clon = (Notificacion) super.clone();
            clon.idNotificacion = 0;
            clon.estado         = EstadoNotificacion.PENDIENTE;
            clon.leida          = false;
            clon.fechaHora      = LocalDateTime.now();
            return clon;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("[Notificacion] Error al clonar: " + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // Getters y Setters
    // --------------------------------------------------------
    public int getIdNotificacion()                      { return idNotificacion; }
    public void setIdNotificacion(int id)               { this.idNotificacion = id; }

    public int getIdUsuario()                           { return idUsuario; }
    public void setIdUsuario(int id)                    { this.idUsuario = id; }

    public TipoNotificacion getTipo()                   { return tipo; }
    public void setTipo(TipoNotificacion tipo)          { this.tipo = tipo; }

    public Integer getIdTransferencia()                 { return idTransferencia; }
    public void setIdTransferencia(Integer id)          { this.idTransferencia = id; }

    public String getMensaje()                          { return mensaje; }
    public void setMensaje(String mensaje)              { this.mensaje = mensaje; }

    public String getCanal()                            { return canal; }
    public void setCanal(String canal)                  { this.canal = canal; }

    public EstadoNotificacion getEstado()               { return estado; }
    public void setEstado(EstadoNotificacion estado)    { this.estado = estado; }

    public boolean isLeida()                            { return leida; }
    public void setLeida(boolean leida)                 { this.leida = leida; }

    public LocalDateTime getFechaHora()                 { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora)   { this.fechaHora = fechaHora; }

    @Override
    public String toString() {
        return "Notificacion{id=" + idNotificacion
               + ", tipo=" + tipo
               + ", canal='" + canal + "'"
               + ", estado=" + estado
               + ", leida=" + leida + "}";
    }
}
