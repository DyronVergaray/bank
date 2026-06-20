package model;

/**
 * ============================================================
 * PATRÓN: PROTOTYPE
 * ============================================================
 * Representa una entidad bancaria disponible para vincular
 * (BCP, BBVA, Interbank, o cualquier otra que el admin agregue).
 * Mapea la tabla dbo.Tipo_Cuenta_Banco.
 *
 * Implementa Cloneable porque, al crear un nuevo tipo de cuenta
 * desde el panel de administración, se parte de una PLANTILLA
 * BASE (valores por defecto de api_endpoint/api_key) y se clona
 * para luego personalizar solo el nombre de la entidad. Esto
 * evita reconstruir el objeto desde cero cada vez y centraliza
 * los valores por defecto en un único lugar.
 *
 * Clase: TipoCuentaBancaria
 * Módulo: Vinculación de Cuentas Bancarias — QoriBank
 * ============================================================
 */
public class TipoCuentaBancaria implements Cloneable {

    // --- Atributos mapeados a dbo.Tipo_Cuenta_Banco ---
    private int     idTipoCuenta;
    private String  nombreEntidad;
    private String  apiEndpoint;
    private String  apiKey;
    private boolean activo;

    // --------------------------------------------------------
    // Constructor vacío
    // --------------------------------------------------------
    public TipoCuentaBancaria() {
        this.activo = true;
    }

    public TipoCuentaBancaria(String nombreEntidad, String apiEndpoint, String apiKey) {
        this.nombreEntidad = nombreEntidad;
        this.apiEndpoint   = apiEndpoint;
        this.apiKey        = apiKey;
        this.activo        = true;
    }

    // --------------------------------------------------------
    // PATRÓN PROTOTYPE: plantilla base reutilizable
    // --------------------------------------------------------
    // Plantilla con valores de API genéricos por defecto, usada
    // como punto de partida cuando el admin registra una nueva
    // entidad bancaria sin especificar manualmente cada campo.
    private static final TipoCuentaBancaria PLANTILLA_BASE =
            new TipoCuentaBancaria("NUEVA_ENTIDAD",
                    "https://api.NUEVA_ENTIDAD.pe/v1/cuentas",
                    "demo_sk_pendiente_de_configurar");

    /**
     * PROTOTYPE: retorna una copia clonada de la plantilla base,
     * lista para personalizar (nombre, endpoint y key reales)
     * antes de persistirla.
     */
    public static TipoCuentaBancaria desdePlantilla() {
        return PLANTILLA_BASE.clone();
    }

    @Override
    public TipoCuentaBancaria clone() {
        try {
            return (TipoCuentaBancaria) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("[TipoCuentaBancaria] Error al clonar plantilla: " + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // Getters y Setters
    // --------------------------------------------------------
    public int getIdTipoCuenta()                    { return idTipoCuenta; }
    public void setIdTipoCuenta(int idTipoCuenta)   { this.idTipoCuenta = idTipoCuenta; }

    public String getNombreEntidad()                { return nombreEntidad; }
    public void setNombreEntidad(String v)          { this.nombreEntidad = v; }

    public String getApiEndpoint()                  { return apiEndpoint; }
    public void setApiEndpoint(String apiEndpoint)  { this.apiEndpoint = apiEndpoint; }

    public String getApiKey()                       { return apiKey; }
    public void setApiKey(String apiKey)            { this.apiKey = apiKey; }

    public boolean isActivo()                       { return activo; }
    public void setActivo(boolean activo)           { this.activo = activo; }

    @Override
    public String toString() {
        return "TipoCuentaBancaria{id=" + idTipoCuenta
               + ", entidad='" + nombreEntidad + "'"
               + ", activo=" + activo + "}";
    }
}
