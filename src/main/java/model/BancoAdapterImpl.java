package model;

import java.math.BigDecimal;
import java.security.SecureRandom;

/**
 * ============================================================
 * PATRÓN: ADAPTER (implementación concreta — BCP)
 * ============================================================
 * Simula la integración con la API del Banco de Crédito del Perú.
 * En producción, aquí se llamaría al endpoint real configurado
 * en dbo.Tipo_Cuenta_Banco (api_endpoint + api_key).
 *
 * Clase: BCPAdapter
 * ============================================================
 */
class BCPAdapter implements BancoAdapter {

    private final String apiEndpoint;
    private final String apiKey;
    private static final SecureRandom RANDOM = new SecureRandom();

    public BCPAdapter(String apiEndpoint, String apiKey) {
        this.apiEndpoint = apiEndpoint;
        this.apiKey      = apiKey;
    }

    @Override
    public boolean validarCuenta(String numeroTarjeta, String fechaVencimiento, String cvv) {
        // --- SIMULACIÓN DE LLAMADA A LA API DE BCP ---
        // En producción: POST a apiEndpoint con Authorization: Bearer apiKey
        System.out.println("[BCPAdapter] Validando tarjeta vía " + apiEndpoint
                + " (apiKey=" + apiKey + ")");
        // Validación simulada: número de tarjeta debe tener 16 dígitos
        return numeroTarjeta != null && numeroTarjeta.replaceAll("\\D", "").length() == 16;
    }

    @Override
    public BigDecimal consultarSaldo(String numeroTarjeta) {
        // Saldo simulado, reservado para el módulo de transferencias
        return BigDecimal.valueOf(RANDOM.nextInt(5000));
    }

    @Override
    public String getNombreEntidad() {
        return "BCP";
    }
}


/**
 * ============================================================
 * PATRÓN: ADAPTER (implementación concreta — BBVA)
 * ============================================================
 * Simula la integración con la API de BBVA Perú.
 *
 * Clase: BBVAAdapter
 * ============================================================
 */
class BBVAAdapter implements BancoAdapter {

    private final String apiEndpoint;
    private final String apiKey;
    private static final SecureRandom RANDOM = new SecureRandom();

    public BBVAAdapter(String apiEndpoint, String apiKey) {
        this.apiEndpoint = apiEndpoint;
        this.apiKey      = apiKey;
    }

    @Override
    public boolean validarCuenta(String numeroTarjeta, String fechaVencimiento, String cvv) {
        // --- SIMULACIÓN DE LLAMADA A LA API DE BBVA ---
        System.out.println("[BBVAAdapter] Validando tarjeta vía " + apiEndpoint
                + " (apiKey=" + apiKey + ")");
        return numeroTarjeta != null && numeroTarjeta.replaceAll("\\D", "").length() == 16;
    }

    @Override
    public BigDecimal consultarSaldo(String numeroTarjeta) {
        return BigDecimal.valueOf(RANDOM.nextInt(5000));
    }

    @Override
    public String getNombreEntidad() {
        return "BBVA";
    }
}


/**
 * ============================================================
 * PATRÓN: ADAPTER (implementación concreta — Interbank)
 * ============================================================
 * Simula la integración con la API de Interbank.
 *
 * Clase: InterbankAdapter
 * ============================================================
 */
class InterbankAdapter implements BancoAdapter {

    private final String apiEndpoint;
    private final String apiKey;
    private static final SecureRandom RANDOM = new SecureRandom();

    public InterbankAdapter(String apiEndpoint, String apiKey) {
        this.apiEndpoint = apiEndpoint;
        this.apiKey      = apiKey;
    }

    @Override
    public boolean validarCuenta(String numeroTarjeta, String fechaVencimiento, String cvv) {
        // --- SIMULACIÓN DE LLAMADA A LA API DE INTERBANK ---
        System.out.println("[InterbankAdapter] Validando tarjeta vía " + apiEndpoint
                + " (apiKey=" + apiKey + ")");
        return numeroTarjeta != null && numeroTarjeta.replaceAll("\\D", "").length() == 16;
    }

    @Override
    public BigDecimal consultarSaldo(String numeroTarjeta) {
        return BigDecimal.valueOf(RANDOM.nextInt(5000));
    }

    @Override
    public String getNombreEntidad() {
        return "Interbank";
    }
}


/**
 * ============================================================
 * PATRÓN: ADAPTER (implementación genérica)
 * ============================================================
 * Usado cuando el admin agrega una entidad bancaria NUEVA que no
 * tiene una clase Java dedicada (BCP/BBVA/Interbank). Este
 * adapter genérico funciona para cualquier banco dado de alta
 * dinámicamente, usando únicamente los datos guardados en
 * dbo.Tipo_Cuenta_Banco (nombre, endpoint, api_key).
 *
 * Clase: BancoGenericoAdapter
 * ============================================================
 */
class BancoGenericoAdapter implements BancoAdapter {

    private final String nombreEntidad;
    private final String apiEndpoint;
    private final String apiKey;
    private static final SecureRandom RANDOM = new SecureRandom();

    public BancoGenericoAdapter(String nombreEntidad, String apiEndpoint, String apiKey) {
        this.nombreEntidad = nombreEntidad;
        this.apiEndpoint   = apiEndpoint;
        this.apiKey        = apiKey;
    }

    @Override
    public boolean validarCuenta(String numeroTarjeta, String fechaVencimiento, String cvv) {
        // --- SIMULACIÓN DE LLAMADA A UNA API BANCARIA GENÉRICA ---
        System.out.println("[BancoGenericoAdapter:" + nombreEntidad + "] Validando tarjeta vía "
                + apiEndpoint + " (apiKey=" + apiKey + ")");
        return numeroTarjeta != null && numeroTarjeta.replaceAll("\\D", "").length() == 16;
    }

    @Override
    public BigDecimal consultarSaldo(String numeroTarjeta) {
        return BigDecimal.valueOf(RANDOM.nextInt(5000));
    }

    @Override
    public String getNombreEntidad() {
        return nombreEntidad;
    }
}
