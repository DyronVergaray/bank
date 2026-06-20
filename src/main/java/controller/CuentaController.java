package controller;

import model.*;
import util.SeguridadUtil;

import java.util.List;

/**
 * ============================================================
 * CAPA: CONTROLLER — Cuentas Bancarias
 * ============================================================
 * Gestiona el flujo de vinculación de cuentas bancarias del
 * Módulo 2:
 *   1. Registrar una tarjeta (número, vencimiento, CVV) en
 *      estado PENDIENTE
 *   2. Enviar código de verificación OTP (reutiliza el mismo
 *      flujo SMS/Correo del Módulo 1)
 *   3. Verificar el código → la cuenta pasa a VINCULADA
 *
 * También expone las operaciones de administración de tipos
 * de cuenta bancaria (crear/eliminar entidades).
 *
 * Patrones en uso:
 *   - FACTORY    → CuentaBancariaFactory (crea el BancoAdapter correcto)
 *   - ADAPTER    → BancoAdapter (BCP/BBVA/Interbank/Genérico)
 *   - STATE      → CuentaBancaria.EstadoCuenta
 *   - OBSERVER   → CuentaBancariaObserver (notifica vinculación/error)
 *   - PROTOTYPE  → TipoCuentaBancaria.desdePlantilla()
 *   - SINGLETON  → ConexionBD (heredado vía CuentaBancariaDAO)
 *
 * Clase: CuentaController
 * Módulo: Vinculación de Cuentas Bancarias — QoriBank
 * ============================================================
 */
public class CuentaController {

    private final CuentaBancariaDAO cuentaDAO;

    // Usuario propietario de las cuentas gestionadas en esta sesión
    private final Usuario usuarioActual;

    // Cuenta bancaria en proceso de vinculación (estado PENDIENTE)
    private CuentaBancaria cuentaPendiente;
    private int            idSesionVinculacion;
    private String         codigoGenerado;

    /**
     * @param usuarioActual  Usuario ya autenticado (login completo).
     *                        Solo un CLIENTE puede registrar cuentas;
     *                        la vista es responsable de no exponer
     *                        esta opción a un ADMIN.
     */
    public CuentaController(Usuario usuarioActual) {
        this.cuentaDAO     = new CuentaBancariaDAO();
        this.usuarioActual = usuarioActual;
    }

    // ============================================================
    // PASO 1: Registrar una nueva tarjeta (queda en PENDIENTE)
    // ============================================================

    /**
     * Valida y registra una nueva cuenta bancaria para el usuario
     * actual. La tarjeta queda en estado PENDIENTE hasta que se
     * verifique el código OTP.
     *
     * @param idTipoCuenta      Id de la entidad bancaria elegida
     * @param numeroTarjeta     16 dígitos en texto plano
     * @param fechaVencimiento  Formato MM/AAAA
     * @param cvv               3 o 4 dígitos en texto plano
     * @param saldoActual       Saldo actual declarado por el usuario para esa tarjeta
     * Retorna null si fue exitoso, o un mensaje de error.
     */
    public String registrarCuenta(int idTipoCuenta, String numeroTarjeta,
                                  String fechaVencimiento, String cvv,
                                  java.math.BigDecimal saldoActual) {

        if (usuarioActual == null)
            return "Debe iniciar sesión para registrar una cuenta bancaria.";

        // Validaciones básicas (controlador)
        if (!SeguridadUtil.numeroTarjetaValido(numeroTarjeta))
            return "El número de tarjeta debe tener 16 dígitos.";
        if (!SeguridadUtil.fechaVencimientoValida(fechaVencimiento))
            return "La fecha de vencimiento no es válida o ya expiró (formato MM/AAAA).";
        if (!SeguridadUtil.cvvValido(cvv))
            return "El CVV debe tener 3 o 4 dígitos.";
        if (saldoActual == null || saldoActual.signum() < 0)
            return "Ingrese un saldo válido (no puede ser negativo).";

        // PATRÓN FACTORY + ADAPTER: validar la tarjeta ante la "API" del
        // banco correspondiente, usando los datos en claro mientras aún
        // están disponibles en memoria (nunca se persisten así).
        TipoCuentaBancaria tipoCuenta = obtenerTipoCuentaDe(idTipoCuenta);
        if (tipoCuenta == null)
            return "La entidad bancaria seleccionada no es válida.";

        BancoAdapter adapter = CuentaBancariaFactory.crearAdapter(tipoCuenta);
        boolean cuentaValidaAnteElBanco = adapter.validarCuenta(numeroTarjeta, fechaVencimiento, cvv);
        if (!cuentaValidaAnteElBanco)
            return "El banco " + tipoCuenta.getNombreEntidad() + " no pudo validar esta tarjeta.";

        // Hash del número de tarjeta y del CVV (nunca se guardan en claro)
        String tarjetaHash = SeguridadUtil.hashSHA256(numeroTarjeta);
        String cvvHash      = SeguridadUtil.hashSHA256(cvv);

        // Regla de negocio: una tarjeta solo puede pertenecer a un usuario
        if (cuentaDAO.existeTarjeta(tarjetaHash))
            return "Esta tarjeta ya se encuentra registrada en el sistema.";

        String enmascarado = SeguridadUtil.enmascararTarjeta(numeroTarjeta);

        int idCuenta = cuentaDAO.registrarCuentaBancaria(
                usuarioActual.getIdUsuario(), idTipoCuenta,
                tarjetaHash, enmascarado, fechaVencimiento, cvvHash, saldoActual);

        if (idCuenta == -1)
            return "Esta tarjeta ya se encuentra registrada en el sistema.";
        if (idCuenta <= 0)
            return "Error al registrar la cuenta bancaria. Intente nuevamente.";

        // Construir el objeto en memoria para continuar el flujo OTP
        cuentaPendiente = new CuentaBancaria(usuarioActual.getIdUsuario(), idTipoCuenta,
                tarjetaHash, enmascarado, fechaVencimiento, cvvHash);
        cuentaPendiente.setIdCuentaBancaria(idCuenta);
        cuentaPendiente.setSaldo(saldoActual);

        return null;
    }

    // ============================================================
    // PASO 2: Enviar código de verificación OTP para la vinculación
    // ============================================================

    /**
     * Envía el código OTP (SMS o Correo) para confirmar que la
     * tarjeta registrada pertenece al usuario.
     * Retorna null si fue exitoso, o un mensaje de error.
     */
    public String enviarCodigoVinculacion(InicioSesion.CanalVerificacion canal) {

        if (cuentaPendiente == null)
            return "No hay una cuenta bancaria pendiente de verificación.";

        codigoGenerado    = SeguridadUtil.generarCodigo6Digitos();
        String codigoHash = SeguridadUtil.hashSHA256(codigoGenerado);

        idSesionVinculacion = cuentaDAO.crearSesionVinculacionCuenta(
                usuarioActual.getIdUsuario(), canal.getIdTipo(),
                cuentaPendiente.getIdCuentaBancaria(), codigoHash);

        if (idSesionVinculacion < 0)
            return "Error al generar el código de verificación. Intente nuevamente.";

        String destinatario = (canal == InicioSesion.CanalVerificacion.SMS)
                ? usuarioActual.getTelefono()
                : usuarioActual.getEmail();

        // PATRÓN FACTORY + OBSERVER: crear notificador y enviar el código
        NotificacionObserver notificador = NotificacionFactory.crear(canal);
        notificador.enviarCodigo(destinatario, codigoGenerado, usuarioActual.getPrimerNombre());

        return null;
    }

    // ============================================================
    // PASO 3: Verificar el código y vincular la cuenta
    // ============================================================

    /**
     * Verifica el código OTP ingresado. Si es correcto, la cuenta
     * bancaria pasa a estado VINCULADA (PATRÓN STATE) y se notifica
     * al usuario (PATRÓN OBSERVER). La validación ante el banco vía
     * ADAPTER ya se realizó en registrarCuenta() (paso 1).
     *
     * Retorna null si fue exitoso, o un mensaje de error.
     */
    public String verificarCodigoVinculacion(String codigoIngresado,
                                             InicioSesion.CanalVerificacion canal) {

        if (cuentaPendiente == null)
            return "No hay una cuenta bancaria pendiente de verificación.";
        if (codigoIngresado == null || codigoIngresado.trim().isEmpty())
            return "Ingrese el código de verificación.";

        String hashIngresado = SeguridadUtil.hashSHA256(codigoIngresado.trim());
        int idCuentaVerificada = cuentaDAO.verificarCodigoVinculacion(idSesionVinculacion, hashIngresado);

        String destinatario = (canal == InicioSesion.CanalVerificacion.SMS)
                ? usuarioActual.getTelefono()
                : usuarioActual.getEmail();
        CuentaBancariaObserver observador =
                new NotificacionCuentaBancariaObserver(canal, destinatario);

        if (idCuentaVerificada <= 0) {
            // PATRÓN STATE: transición a ERROR
            cuentaPendiente.marcarError();
            cuentaDAO.actualizarEstadoCuentaBancaria(
                    cuentaPendiente.getIdCuentaBancaria(), "ERROR");

            // PATRÓN OBSERVER: notificar el fallo
            observador.onErrorSincronizacion(cuentaPendiente,
                    usuarioActual.getPrimerNombre(), "código incorrecto o expirado");

            return "Código incorrecto o expirado. Solicite uno nuevo.";
        }

        // PATRÓN STATE: transición a VINCULADA (la validación ante el
        // banco vía ADAPTER ya ocurrió en registrarCuenta(), paso 1)
        cuentaPendiente.vincular();

        // PATRÓN OBSERVER: notificar la vinculación exitosa
        observador.onCuentaVinculada(cuentaPendiente, usuarioActual.getPrimerNombre());

        return null;
    }

    // ============================================================
    // CONSULTAS
    // ============================================================

    /** Lista todas las cuentas bancarias del usuario actual. */
    public List<CuentaBancaria> obtenerCuentasDelUsuario() {
        if (usuarioActual == null) return List.of();
        return cuentaDAO.obtenerCuentasPorUsuario(usuarioActual.getIdUsuario());
    }

    /** Lista las entidades bancarias disponibles para vincular. */
    public List<TipoCuentaBancaria> obtenerTiposCuentaDisponibles() {
        return cuentaDAO.obtenerTiposCuentaBanco();
    }

    /** Cancela el registro pendiente y limpia el estado en memoria. */
    public void cancelarRegistroPendiente() {
        if (cuentaPendiente != null) {
            cuentaDAO.eliminarCuentaBancariaPendiente(cuentaPendiente.getIdCuentaBancaria());
        }
        cuentaPendiente      = null;
        idSesionVinculacion  = -1;
        codigoGenerado       = null;
    }

    private TipoCuentaBancaria obtenerTipoCuentaDe(int idTipoCuenta) {
        return cuentaDAO.obtenerTiposCuentaBanco().stream()
                .filter(t -> t.getIdTipoCuenta() == idTipoCuenta)
                .findFirst()
                .orElse(null);
    }

    // ============================================================
    // ADMINISTRACIÓN DE TIPOS DE CUENTA BANCARIA (solo ADMIN)
    // ============================================================

    /**
     * Crea un nuevo tipo de cuenta bancaria (entidad) a partir de
     * la PLANTILLA BASE (PATRÓN PROTOTYPE), personalizando nombre,
     * endpoint y api_key.
     * Retorna null si fue exitoso, o un mensaje de error.
     */
    public String crearTipoCuentaBanco(String nombreEntidad, String apiEndpoint, String apiKey) {
        if (nombreEntidad == null || nombreEntidad.trim().isEmpty())
            return "Ingrese el nombre de la entidad bancaria.";
        if (apiEndpoint == null || apiEndpoint.trim().isEmpty())
            return "Ingrese el endpoint de la API.";
        if (apiKey == null || apiKey.trim().isEmpty())
            return "Ingrese la API key.";

        // PATRÓN PROTOTYPE: partir de la plantilla y personalizarla
        TipoCuentaBancaria nuevoTipo = TipoCuentaBancaria.desdePlantilla();
        nuevoTipo.setNombreEntidad(nombreEntidad.trim());
        nuevoTipo.setApiEndpoint(apiEndpoint.trim());
        nuevoTipo.setApiKey(apiKey.trim());

        int resultado = cuentaDAO.crearTipoCuentaBanco(
                nuevoTipo.getNombreEntidad(), nuevoTipo.getApiEndpoint(), nuevoTipo.getApiKey());

        if (resultado > 0) return null;
        if (resultado == -1) return "Ya existe una entidad bancaria con ese nombre.";
        return "Error interno al crear la entidad bancaria. Intente nuevamente.";
    }

    /**
     * Elimina (o desactiva, si tiene cuentas vinculadas) un tipo
     * de cuenta bancaria.
     * Retorna null si fue exitoso, o un mensaje de error.
     */
    public String eliminarTipoCuentaBanco(int idTipoCuenta) {
        boolean ok = cuentaDAO.eliminarTipoCuentaBanco(idTipoCuenta);
        return ok ? null : "No se pudo eliminar la entidad bancaria.";
    }

    // ============================================================
    // GETTERS DE ESTADO
    // ============================================================

    public CuentaBancaria getCuentaPendiente()        { return cuentaPendiente; }
    public String         getCodigoGeneradoSimulacion() { return codigoGenerado; }
}
