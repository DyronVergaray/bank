package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * ============================================================
 * CLASE UTILITARIA: SeguridadUtil
 * ============================================================
 * Provee funciones de seguridad reutilizables:
 *   - Hash SHA-256 para contraseñas y códigos de verificación
 *   - Generación de códigos numéricos de 6 dígitos
 *   - Generación de tokens UUID para sesiones
 *
 * Clase: SeguridadUtil
 * Módulo: Gestión de Usuarios y Autenticación — QoriBank
 * ============================================================
 */
public class SeguridadUtil {

    // Generador criptográficamente seguro
    private static final SecureRandom RANDOM = new SecureRandom();

    // --------------------------------------------------------
    // Genera el hash SHA-256 de un texto en claro.
    // Retorna la representación hexadecimal en minúsculas.
    // --------------------------------------------------------
    public static String hashSHA256(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes     = digest.digest(texto.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("[SeguridadUtil] SHA-256 no disponible: " + e.getMessage());
        }
    }

    // --------------------------------------------------------
    // Genera un código numérico aleatorio de 6 dígitos.
    // Ejemplo: "047823"
    // --------------------------------------------------------
    public static String generarCodigo6Digitos() {
        int codigo = RANDOM.nextInt(900000) + 100000; // 100000 – 999999
        return String.valueOf(codigo);
    }

    // --------------------------------------------------------
    // Genera un token de sesión único basado en UUID v4.
    // Ejemplo: "3f2504e0-4f89-11d3-9a0c-0305e82c3301"
    // --------------------------------------------------------
    public static String generarTokenSesion() {
        return UUID.randomUUID().toString();
    }

    // --------------------------------------------------------
    // Valida que el email tenga un formato básico correcto.
    // --------------------------------------------------------
    public static boolean emailValido(String email) {
        return email != null && email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    }

    // --------------------------------------------------------
    // Valida que la contraseña tenga al menos 6 caracteres.
    // --------------------------------------------------------
    public static boolean passwordValida(String password) {
        return password != null && password.length() >= 6;
    }

    // --------------------------------------------------------
    // Valida que el teléfono ingresado sean exactamente 9 dígitos
    // numéricos (sin código de país, sin espacios ni símbolos).
    // Ejemplo válido: "987654321"
    // --------------------------------------------------------
    public static boolean telefonoValido(String telefono) {
        return telefono != null && telefono.matches("^\\d{9}$");
    }

    // --------------------------------------------------------
    // Normaliza un teléfono de 9 dígitos al formato con código
    // de país de Perú: "+51" + 9 dígitos.
    // Ejemplo: "987654321" → "+51987654321"
    // --------------------------------------------------------
    public static String normalizarTelefono(String telefono) {
        return "+51" + telefono;
    }

    // --------------------------------------------------------
    // Valida que el número de tarjeta tenga exactamente 16
    // dígitos numéricos (sin espacios ni guiones).
    // --------------------------------------------------------
    public static boolean numeroTarjetaValido(String numeroTarjeta) {
        return numeroTarjeta != null && numeroTarjeta.matches("^\\d{16}$");
    }

    // --------------------------------------------------------
    // Valida el formato de fecha de vencimiento MM/AAAA y que
    // no esté ya vencida respecto a la fecha actual.
    // --------------------------------------------------------
    public static boolean fechaVencimientoValida(String mmAAAA) {
        if (mmAAAA == null || !mmAAAA.matches("^(0[1-9]|1[0-2])/\\d{4}$")) {
            return false;
        }
        int mes  = Integer.parseInt(mmAAAA.substring(0, 2));
        int anio = Integer.parseInt(mmAAAA.substring(3));

        java.time.YearMonth vencimiento = java.time.YearMonth.of(anio, mes);
        java.time.YearMonth actual      = java.time.YearMonth.now();
        return !vencimiento.isBefore(actual);
    }

    // --------------------------------------------------------
    // Valida que el CVV tenga 3 o 4 dígitos numéricos.
    // --------------------------------------------------------
    public static boolean cvvValido(String cvv) {
        return cvv != null && cvv.matches("^\\d{3,4}$");
    }

    // --------------------------------------------------------
    // Enmascara un número de tarjeta de 16 dígitos, mostrando
    // solo los últimos 4. Ejemplo: "**** **** **** 4321"
    // --------------------------------------------------------
    public static String enmascararTarjeta(String numeroTarjeta) {
        if (numeroTarjeta == null || numeroTarjeta.length() < 4) return "**** **** **** ****";
        String ultimos4 = numeroTarjeta.substring(numeroTarjeta.length() - 4);
        return "**** **** **** " + ultimos4;
    }
}
