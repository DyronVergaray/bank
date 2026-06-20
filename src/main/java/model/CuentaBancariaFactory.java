package model;

/**
 * ============================================================
 * PATRÓN: FACTORY
 * ============================================================
 * Centraliza la creación del BancoAdapter correspondiente según
 * la entidad bancaria elegida por el usuario (BCP, BBVA,
 * Interbank, o cualquier banco nuevo agregado por el admin).
 *
 * El controlador no necesita saber qué clase concreta de
 * adapter instanciar: solo le pasa el TipoCuentaBancaria (con su
 * nombre, endpoint y api_key obtenidos de la BD) y la Factory
 * decide internamente qué implementación usar.
 *
 * Clase: CuentaBancariaFactory
 * Módulo: Vinculación de Cuentas Bancarias — QoriBank
 * ============================================================
 */
public class CuentaBancariaFactory {

    /**
     * Crea el adapter bancario correspondiente al tipo de cuenta.
     * Si el nombre de la entidad coincide con uno de los bancos
     * predefinidos (BCP, BBVA, Interbank), retorna su implementación
     * dedicada; de lo contrario, retorna el adapter genérico — esto
     * permite que el admin agregue nuevos bancos sin requerir
     * cambios de código.
     *
     * @param tipoCuenta  Entidad bancaria (con endpoint y api_key de BD)
     * @return            Implementación concreta de BancoAdapter
     */
    public static BancoAdapter crearAdapter(TipoCuentaBancaria tipoCuenta) {
        if (tipoCuenta == null) {
            throw new IllegalArgumentException("[CuentaBancariaFactory] tipoCuenta no puede ser null");
        }

        String entidad = tipoCuenta.getNombreEntidad();
        String endpoint = tipoCuenta.getApiEndpoint();
        String apiKey    = tipoCuenta.getApiKey();

        if (entidad == null) {
            throw new IllegalArgumentException("[CuentaBancariaFactory] nombreEntidad no puede ser null");
        }

        switch (entidad.toUpperCase()) {
            case "BCP":
                return new BCPAdapter(endpoint, apiKey);
            case "BBVA":
                return new BBVAAdapter(endpoint, apiKey);
            case "INTERBANK":
                return new InterbankAdapter(endpoint, apiKey);
            default:
                // Entidad nueva creada por el admin: adapter genérico
                return new BancoGenericoAdapter(entidad, endpoint, apiKey);
        }
    }
}
