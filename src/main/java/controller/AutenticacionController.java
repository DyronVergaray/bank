package controller;

import model.*;
import util.SeguridadUtil;

/**
 * ============================================================
 * CAPA: CONTROLLER — Autenticación
 * ============================================================
 * Gestiona todos los eventos del flujo de autenticación:
 *   1. Validar credenciales (email + contraseña)
 *   2. Crear sesión pendiente y enviar código de verificación
 *   3. Verificar el código ingresado por el usuario
 *
 * Patrones en uso:
 *   - SINGLETON  → ConexionBD (acceso a BD)
 *   - FACTORY    → NotificacionFactory (crea SMS o Correo)
 *   - OBSERVER   → NotificacionObserver (envía el código)
 *   - STATE      → InicioSesion.EstadoSesion (flujo de estado)
 *   - PROTOTYPE  → Usuario.clone() (perfil temporal en sesión)
 *
 * Clase: AutenticacionController
 * Módulo: Gestión de Usuarios y Autenticación — QoriBank
 * ============================================================
 */
public class AutenticacionController {

    // Acceso a datos (usa SINGLETON internamente)
    private final UsuarioDAO usuarioDAO;

    // Sesión en curso (STATE)
    private InicioSesion sesionActual;

    // Usuario autenticado (PROTOTYPE — copia del perfil)
    private Usuario usuarioActual;

    // Código en texto plano generado (para mostrarlo en la simulación)
    private String codigoGenerado;

    // --------------------------------------------------------
    // Constructor
    // --------------------------------------------------------
    public AutenticacionController() {
        this.usuarioDAO = new UsuarioDAO();
    }

    // --------------------------------------------------------
    // PASO 1: Validar credenciales (email + contraseña)
    //
    // Retorna:
    //   null  → credenciales válidas, continuar con el flujo
    //   String → mensaje de error a mostrar en la Vista
    // --------------------------------------------------------
    public String validarCredenciales(String email, String password) {

        // Validaciones básicas (controlador)
        if (email == null || email.trim().isEmpty()) {
            return "Ingrese su correo electrónico.";
        }
        if (!SeguridadUtil.emailValido(email.trim())) {
            return "El formato del correo no es válido.";
        }
        if (!SeguridadUtil.passwordValida(password)) {
            return "La contraseña debe tener al menos 6 caracteres.";
        }

        // Hash de la contraseña para comparar con BD
        String hashPwd = SeguridadUtil.hashSHA256(password);

        // Consulta a BD via DAO (Stored Procedure sp_ValidarCredenciales)
        Usuario usuario = usuarioDAO.validarCredenciales(email.trim(), hashPwd);

        if (usuario == null) {
            return "Correo o contraseña incorrectos.";
        }

        // PATRÓN PROTOTYPE: almacenar copia del perfil en memoria
        this.usuarioActual = usuario.clone();

        return null; // sin error → credenciales válidas
    }

    // --------------------------------------------------------
    // PASO 2: Enviar código de verificación al canal elegido
    //
    // @param canal  Canal seleccionado por el usuario (SMS o CORREO)
    // Retorna:
    //   null  → código enviado correctamente
    //   String → mensaje de error
    // --------------------------------------------------------
    public String enviarCodigoVerificacion(InicioSesion.CanalVerificacion canal) {

        if (usuarioActual == null) {
            return "Sesión inválida. Inicie el proceso nuevamente.";
        }

        // Generar código de 6 dígitos y su hash para almacenar en BD
        codigoGenerado          = SeguridadUtil.generarCodigo6Digitos();
        String codigoHash       = SeguridadUtil.hashSHA256(codigoGenerado);

        // Guardar sesión pendiente en BD (Stored Procedure sp_CrearSesionConCodigo)
        int idSesion = usuarioDAO.crearSesionConCodigo(
                usuarioActual.getIdUsuario(),
                canal.getIdTipo(),
                codigoHash
        );

        if (idSesion < 0) {
            return "Error al crear la sesión. Intente nuevamente.";
        }

        // PATRÓN STATE: crear objeto de sesión con estado PENDIENTE
        sesionActual = new InicioSesion(usuarioActual.getIdUsuario(), canal);
        sesionActual.setIdSesion(idSesion);
        sesionActual.setCodigoHash(codigoHash);

        // Determinar destinatario según canal
        String destinatario = (canal == InicioSesion.CanalVerificacion.SMS)
                ? usuarioActual.getTelefono()
                : usuarioActual.getEmail();

        // PATRÓN FACTORY: crear el notificador según canal
        NotificacionObserver notificador = NotificacionFactory.crear(canal);

        // PATRÓN OBSERVER: enviar el código
        notificador.enviarCodigo(destinatario, codigoGenerado, usuarioActual.getPrimerNombre());

        return null; // sin error
    }

    // --------------------------------------------------------
    // PASO 3: Verificar el código ingresado por el usuario
    //
    // Retorna:
    //   null  → código correcto, sesión activada
    //   String → mensaje de error
    // --------------------------------------------------------
    public String verificarCodigo(String codigoIngresado) {

        if (sesionActual == null) {
            return "No hay una sesión pendiente.";
        }

        if (codigoIngresado == null || codigoIngresado.trim().isEmpty()) {
            return "Ingrese el código de verificación.";
        }

        // Hash del código ingresado para comparar
        String hashIngresado = SeguridadUtil.hashSHA256(codigoIngresado.trim());

        // Generar token único para la sesión activa
        String tokenSesion = SeguridadUtil.generarTokenSesion();

        // Verificar en BD (Stored Procedure sp_VerificarCodigo)
        boolean correcto = usuarioDAO.verificarCodigo(
                sesionActual.getIdSesion(),
                hashIngresado,
                tokenSesion
        );

        if (!correcto) {
            // PATRÓN STATE: transición a RECHAZADA
            sesionActual.rechazar();
            return "Código incorrecto o expirado. Solicite uno nuevo.";
        }

        // PATRÓN STATE: transición a VERIFICADA
        sesionActual.verificar(tokenSesion, java.time.LocalDateTime.now().plusHours(1));

        return null; // sin error → autenticación exitosa
    }

    // --------------------------------------------------------
    // Obtiene el usuario autenticado actual
    // --------------------------------------------------------
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    // --------------------------------------------------------
    // Obtiene la sesión actual (para inspeccionar estado)
    // --------------------------------------------------------
    public InicioSesion getSesionActual() {
        return sesionActual;
    }

    // --------------------------------------------------------
    // Obtiene el código generado (solo para entorno de pruebas/simulación)
    // En producción este método no debería existir.
    // --------------------------------------------------------
    public String getCodigoGeneradoSimulacion() {
        return codigoGenerado;
    }

    // --------------------------------------------------------
    // Cierra la sesión actual y limpia el estado en memoria
    // --------------------------------------------------------
    public void cerrarSesion() {
        sesionActual  = null;
        usuarioActual = null;
        codigoGenerado = null;
    }
}
