package controller;

import model.*;
import util.SeguridadUtil;

import java.util.List;

/**
 * ============================================================
 * CAPA: CONTROLLER — Autenticación
 * ============================================================
 * Gestiona todos los eventos del flujo de autenticación y
 * las operaciones del panel de administración.
 *
 * Patrones en uso:
 *   - SINGLETON  → ConexionBD
 *   - FACTORY    → NotificacionFactory
 *   - OBSERVER   → NotificacionObserver
 *   - STATE      → InicioSesion.EstadoSesion
 *   - PROTOTYPE  → Usuario.clone()
 *
 * Clase: AutenticacionController
 * Módulo: Gestión de Usuarios y Autenticación — QoriBank
 * ============================================================
 */
public class AutenticacionController {

    private final UsuarioDAO usuarioDAO;

    private InicioSesion sesionActual;
    private Usuario      usuarioActual;
    private String       codigoGenerado;

    public AutenticacionController() {
        this.usuarioDAO = new UsuarioDAO();
    }

    // ============================================================
    // FLUJO DE AUTENTICACIÓN (3 pasos)
    // ============================================================

    // --------------------------------------------------------
    // PASO 1: Validar credenciales (email + contraseña)
    // Retorna null si es correcto, o mensaje de error.
    // --------------------------------------------------------
    public String validarCredenciales(String email, String password) {

        if (email == null || email.trim().isEmpty())
            return "Ingrese su correo electrónico.";

        if (!SeguridadUtil.emailValido(email.trim()))
            return "El formato del correo no es válido.";

        if (!SeguridadUtil.passwordValida(password))
            return "La contraseña debe tener al menos 6 caracteres.";

        String hashPwd = SeguridadUtil.hashSHA256(password);
        Usuario usuario = usuarioDAO.validarCredenciales(email.trim(), hashPwd);

        if (usuario == null)
            return "Correo o contraseña incorrectos.";

        // PATRÓN PROTOTYPE: almacenar copia del perfil en memoria
        this.usuarioActual = usuario.clone();
        return null;
    }

    // --------------------------------------------------------
    // PASO 2: Enviar código de verificación al canal elegido
    // Retorna null si es correcto, o mensaje de error.
    // --------------------------------------------------------
    public String enviarCodigoVerificacion(InicioSesion.CanalVerificacion canal) {

        if (usuarioActual == null)
            return "Sesión inválida. Inicie el proceso nuevamente.";

        codigoGenerado    = SeguridadUtil.generarCodigo6Digitos();
        String codigoHash = SeguridadUtil.hashSHA256(codigoGenerado);

        int idSesion = usuarioDAO.crearSesionConCodigo(
                usuarioActual.getIdUsuario(), canal.getIdTipo(), codigoHash);

        if (idSesion < 0)
            return "Error al crear la sesión. Intente nuevamente.";

        // PATRÓN STATE: objeto de sesión con estado PENDIENTE
        sesionActual = new InicioSesion(usuarioActual.getIdUsuario(), canal);
        sesionActual.setIdSesion(idSesion);
        sesionActual.setCodigoHash(codigoHash);

        String destinatario = (canal == InicioSesion.CanalVerificacion.SMS)
                ? usuarioActual.getTelefono()
                : usuarioActual.getEmail();

        // PATRÓN FACTORY + OBSERVER: crear y enviar notificación
        NotificacionObserver notificador = NotificacionFactory.crear(canal);
        notificador.enviarCodigo(destinatario, codigoGenerado, usuarioActual.getPrimerNombre());

        return null;
    }

    // --------------------------------------------------------
    // PASO 3: Verificar el código ingresado por el usuario
    // Retorna null si es correcto, o mensaje de error.
    // --------------------------------------------------------
    public String verificarCodigo(String codigoIngresado) {

        if (sesionActual == null)
            return "No hay una sesión pendiente.";

        if (codigoIngresado == null || codigoIngresado.trim().isEmpty())
            return "Ingrese el código de verificación.";

        String hashIngresado = SeguridadUtil.hashSHA256(codigoIngresado.trim());
        String tokenSesion   = SeguridadUtil.generarTokenSesion();

        boolean correcto = usuarioDAO.verificarCodigo(
                sesionActual.getIdSesion(), hashIngresado, tokenSesion);

        if (!correcto) {
            sesionActual.rechazar();    // PATRÓN STATE → RECHAZADA
            return "Código incorrecto o expirado. Solicite uno nuevo.";
        }

        sesionActual.verificar(tokenSesion, java.time.LocalDateTime.now().plusHours(1));
        return null;
    }

    // ============================================================
    // REGISTRO DE NUEVO CLIENTE (público)
    // ============================================================

    /**
     * Registra un nuevo usuario con rol CLIENTE.
     * La contraseña se hashea aquí antes de persistirse.
     *
     * Retorna null si fue exitoso, o un mensaje de error.
     */
    public String registrarCliente(String primerNombre, String apellidoPaterno,
                                   String apellidoMaterno, String email,
                                   String telefono, String password) {
        // Validaciones básicas
        if (primerNombre == null || primerNombre.trim().isEmpty())
            return "Ingrese su nombre.";
        if (apellidoPaterno == null || apellidoPaterno.trim().isEmpty())
            return "Ingrese su apellido paterno.";
        if (!SeguridadUtil.emailValido(email))
            return "El formato del correo no es válido.";
        if (telefono == null || telefono.trim().isEmpty())
            return "Ingrese su número de teléfono.";
        if (!SeguridadUtil.passwordValida(password))
            return "La contraseña debe tener al menos 6 caracteres.";

        // Hash de la contraseña antes de persistir
        String hashPwd = SeguridadUtil.hashSHA256(password);

        int resultado = usuarioDAO.crearUsuarioCliente(
                primerNombre.trim(), apellidoPaterno.trim(),
                (apellidoMaterno != null ? apellidoMaterno.trim() : ""),
                email.trim(), telefono.trim(), hashPwd);

        if (resultado > 0) return null;          // éxito
        if (resultado == -1) return "El correo electrónico ya está registrado.";
        if (resultado == -2) return "El número de teléfono ya está registrado.";
        return "Error interno al registrar. Intente nuevamente.";
    }

    // ============================================================
    // OPERACIONES DE ADMINISTRACIÓN
    // ============================================================

    /**
     * Lista todos los usuarios del sistema.
     * Solo debe llamarse desde el panel Admin.
     */
    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioDAO.obtenerTodosUsuarios();
    }

    /**
     * Elimina un usuario por su id.
     * No permite que un admin se elimine a sí mismo.
     *
     * Retorna null si fue exitoso, o un mensaje de error.
     */
    public String eliminarUsuario(int idUsuario) {
        if (usuarioActual != null && usuarioActual.getIdUsuario() == idUsuario)
            return "No puede eliminar su propia cuenta.";

        boolean ok = usuarioDAO.eliminarUsuario(idUsuario);
        return ok ? null : "No se pudo eliminar el usuario.";
    }

    /**
     * Crea un nuevo usuario con rol ADMIN.
     * La contraseña se hashea aquí antes de persistirse.
     *
     * Retorna null si fue exitoso, o un mensaje de error.
     */
    public String crearAdmin(String primerNombre, String apellidoPaterno,
                             String apellidoMaterno, String email,
                             String telefono, String password) {
        // Validaciones básicas
        if (primerNombre == null || primerNombre.trim().isEmpty())
            return "Ingrese el nombre del administrador.";
        if (apellidoPaterno == null || apellidoPaterno.trim().isEmpty())
            return "Ingrese el apellido paterno.";
        if (!SeguridadUtil.emailValido(email))
            return "El formato del correo no es válido.";
        if (telefono == null || telefono.trim().isEmpty())
            return "Ingrese el número de teléfono.";
        if (!SeguridadUtil.passwordValida(password))
            return "La contraseña debe tener al menos 6 caracteres.";

        // Hash de la contraseña antes de persistir
        String hashPwd = SeguridadUtil.hashSHA256(password);

        int resultado = usuarioDAO.crearUsuarioAdmin(
                primerNombre.trim(), apellidoPaterno.trim(),
                (apellidoMaterno != null ? apellidoMaterno.trim() : ""),
                email.trim(), telefono.trim(), hashPwd);

        if (resultado > 0) return null;
        if (resultado == -1) return "El correo electrónico ya está registrado.";
        if (resultado == -2) return "El número de teléfono ya está registrado.";
        return "Error interno al crear administrador. Intente nuevamente.";
    }

    // ============================================================
    // GETTERS DE ESTADO
    // ============================================================

    public Usuario      getUsuarioActual()             { return usuarioActual;  }
    public InicioSesion getSesionActual()              { return sesionActual;   }
    public String       getCodigoGeneradoSimulacion()  { return codigoGenerado; }

    public void cerrarSesion() {
        sesionActual   = null;
        usuarioActual  = null;
        codigoGenerado = null;
    }
}
