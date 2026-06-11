package model;

import java.time.LocalDateTime;

/**
 * ============================================================
 * PATRÓN: STATE
 * ============================================================
 * La sesión de inicio maneja distintos estados a lo largo del
 * flujo de autenticación de dos factores:
 *
 *   PENDIENTE   → Credenciales validadas, código enviado, esperando verificación
 *   VERIFICADA  → Código confirmado, sesión activa con token
 *   EXPIRADA    → El código o la sesión ya no son válidos
 *   RECHAZADA   → Código incorrecto ingresado por el usuario
 *
 * Clase: InicioSesion
 * Módulo: Gestión de Usuarios y Autenticación — QoriBank
 * ============================================================
 */
public class InicioSesion {

    // --------------------------------------------------------
    // PATRÓN STATE: enumeración de estados posibles de sesión
    // --------------------------------------------------------
    public enum EstadoSesion {
        PENDIENTE,    // Código enviado, esperando verificación
        VERIFICADA,   // Sesión autenticada correctamente
        EXPIRADA,     // Tiempo de verificación agotado
        RECHAZADA     // Código incorrecto
    }

    // --------------------------------------------------------
    // Canal de verificación (mapeado a dbo.Tipo_Verificacion)
    // --------------------------------------------------------
    public enum CanalVerificacion {
        SMS("SMS", 1),
        CORREO("CORREO", 2);

        private final String nombre;
        private final int    idTipo;

        CanalVerificacion(String nombre, int idTipo) {
            this.nombre = nombre;
            this.idTipo = idTipo;
        }

        public String getNombre() { return nombre; }
        public int    getIdTipo() { return idTipo;  }
    }

    // --- Atributos mapeados a dbo.Inicio_Sesion ---
    private int               idSesion;
    private int               idUsuario;
    private CanalVerificacion canal;
    private String            codigoHash;
    private boolean           codigoVerificado;
    private LocalDateTime     codigoExpiraEn;
    private String            tokenSesion;
    private LocalDateTime     sesionExpiraEn;
    private LocalDateTime     creadoEn;

    // Estado actual de la sesión (PATRÓN STATE)
    private EstadoSesion estado;

    // --------------------------------------------------------
    // Constructor
    // --------------------------------------------------------
    public InicioSesion(int idUsuario, CanalVerificacion canal) {
        this.idUsuario = idUsuario;
        this.canal     = canal;
        this.estado    = EstadoSesion.PENDIENTE;  // Estado inicial
        this.creadoEn  = LocalDateTime.now();
    }

    // --------------------------------------------------------
    // PATRÓN STATE: transiciones de estado
    // --------------------------------------------------------

    /** Avanza al estado VERIFICADA cuando el código es correcto */
    public void verificar(String tokenSesion, LocalDateTime expiracion) {
        this.codigoVerificado = true;
        this.tokenSesion      = tokenSesion;
        this.sesionExpiraEn   = expiracion;
        this.estado           = EstadoSesion.VERIFICADA;
    }

    /** Transiciona al estado EXPIRADA */
    public void expirar() {
        this.estado = EstadoSesion.EXPIRADA;
    }

    /** Transiciona al estado RECHAZADA */
    public void rechazar() {
        this.estado = EstadoSesion.RECHAZADA;
    }

    /** Verifica si la sesión está activa */
    public boolean estaActiva() {
        return estado == EstadoSesion.VERIFICADA
               && sesionExpiraEn != null
               && LocalDateTime.now().isBefore(sesionExpiraEn);
    }

    /** Verifica si el código aún no ha expirado */
    public boolean codigoVigente() {
        return codigoExpiraEn != null
               && LocalDateTime.now().isBefore(codigoExpiraEn);
    }

    // --------------------------------------------------------
    // Getters y Setters
    // --------------------------------------------------------
    public int getIdSesion()                    { return idSesion;          }
    public void setIdSesion(int idSesion)       { this.idSesion = idSesion; }

    public int getIdUsuario()                   { return idUsuario;         }

    public CanalVerificacion getCanal()         { return canal;             }

    public String getCodigoHash()               { return codigoHash;        }
    public void setCodigoHash(String codigoHash){ this.codigoHash = codigoHash; }

    public boolean isCodigoVerificado()         { return codigoVerificado;  }

    public LocalDateTime getCodigoExpiraEn()    { return codigoExpiraEn;    }
    public void setCodigoExpiraEn(LocalDateTime t) { this.codigoExpiraEn = t; }

    public String getTokenSesion()              { return tokenSesion;       }

    public LocalDateTime getSesionExpiraEn()    { return sesionExpiraEn;    }

    public LocalDateTime getCreadoEn()          { return creadoEn;          }

    public EstadoSesion getEstado()             { return estado;            }
    public void setEstado(EstadoSesion estado)  { this.estado = estado;     }
}
