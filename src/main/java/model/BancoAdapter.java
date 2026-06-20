package model;

/**
 * ============================================================
 * PATRÓN: ADAPTER
 * ============================================================
 * Cada entidad bancaria (BCP, BBVA, Interbank, o cualquier banco
 * que el admin agregue después) tendría en la realidad su propia
 * API con su propio formato de request/response. Esta interfaz
 * define el contrato ÚNICO que el resto del sistema conoce,
 * independientemente del banco real detrás.
 *
 * Las clases concretas (BCPAdapter, BBVAAdapter, etc.) son las
 * encargadas de "adaptar" la llamada genérica a las particulari-
 * dades simuladas de cada API bancaria.
 *
 * Interfaz: BancoAdapter
 * Módulo: Vinculación de Cuentas Bancarias — QoriBank
 * ============================================================
 */
public interface BancoAdapter {

    /**
     * Valida ante el banco que la tarjeta (número, vencimiento, CVV)
     * es una cuenta real y activa. En este proyecto la respuesta
     * es simulada, pero respeta la forma en que una integración
     * real respondería (true/false según la "API" del banco).
     *
     * @param numeroTarjeta   Número de tarjeta en texto plano (solo en memoria)
     * @param fechaVencimiento Formato MM/AAAA
     * @param cvv              Código de seguridad en texto plano (solo en memoria)
     * @return true si el banco confirma que la cuenta es válida
     */
    boolean validarCuenta(String numeroTarjeta, String fechaVencimiento, String cvv);

    /**
     * Consulta el saldo actual de la cuenta ante el banco.
     * Reservado para el módulo de transferencias (saldo simulado
     * por ahora).
     */
    java.math.BigDecimal consultarSaldo(String numeroTarjeta);

    /**
     * Nombre de la entidad bancaria que implementa este adapter,
     * usado para logging y trazabilidad.
     */
    String getNombreEntidad();
}
