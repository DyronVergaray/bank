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
}
