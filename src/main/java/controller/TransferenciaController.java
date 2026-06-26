package controller;

import model.*;
import util.SeguridadUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * ============================================================
 * CAPA: CONTROLLER — Transferencias
 * ============================================================
 * Coordina el flujo de transferencias del Módulo 3:
 *   1. Validar datos y fondos
 *   2. Registrar la transferencia en PENDIENTE (FACTORY)
 *   3. Enviar código OTP de confirmación
 *   4. Delegar el procesamiento final a TransferenciaFacade
 *      (FACADE → STATE + OBSERVER internamente)
 *
 * Clase: TransferenciaController
 * Módulo: Transferencias — QoriBank
 * ============================================================
 */
public class TransferenciaController {

    private final TransferenciaDAO  transferenciaDAO;
    private final CuentaBancariaDAO cuentaDAO;
    private final TransferenciaFacade facade;
    private final Usuario           usuarioActual;

    // Transferencia en curso
    private Transferencia transferenciaActual;
    private int           idSesionTransferencia;
    private String        codigoGenerado;

    public TransferenciaController(Usuario usuarioActual) {
        this.usuarioActual    = usuarioActual;
        this.transferenciaDAO = new TransferenciaDAO();
        this.cuentaDAO        = new CuentaBancariaDAO();
        this.facade           = new TransferenciaFacade();
    }

    // ============================================================
    // PASO 1: Registrar transferencia (queda PENDIENTE)
    // ============================================================

    /**
     * Valida los datos, verifica fondos y registra la transferencia
     * en estado PENDIENTE usando FACTORY para crear el tipo correcto.
     *
     * @param tipo              Tipo de transferencia
     * @param idCuentaOrigen    Id de la cuenta bancaria origen
     * @param numeroDestino     Número de tarjeta destino (texto plano, 16 dígitos)
     * @param entidadDestino    Nombre del banco destino
     * @param monto             Monto a transferir
     * @param descripcion       Concepto/descripción (puede ser vacío)
     * Retorna null si fue exitoso, o mensaje de error.
     */
    public String registrarTransferencia(Transferencia.TipoTransferencia tipo,
                                         int idCuentaOrigen,
                                         String numeroDestino,
                                         String entidadDestino,
                                         BigDecimal monto,
                                         String descripcion) {

        if (usuarioActual == null)
            return "Debe iniciar sesión para realizar transferencias.";

        // Validaciones básicas
        if (!SeguridadUtil.numeroTarjetaValido(numeroDestino))
            return "El número de tarjeta destino debe tener 16 dígitos.";
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0)
            return "El monto debe ser mayor a 0.";
        if (monto.compareTo(new BigDecimal("50000")) > 0)
            return "El monto máximo por transferencia es S/ 50,000.00.";

        // Obtener cuenta origen para enmascarado
        CuentaBancaria cuentaOrigen = cuentaDAO.obtenerCuentaBancariaPorId(idCuentaOrigen);
        if (cuentaOrigen == null || !cuentaOrigen.estaVinculada())
            return "La cuenta de origen no está disponible.";

        // Hash y enmascarado del destino
        String destinoHash = SeguridadUtil.hashSHA256(numeroDestino);
        String destinoEnm  = SeguridadUtil.enmascararTarjeta(numeroDestino);

        // Para ENTRE_CUENTAS: verificar que el destino sea una cuenta propia distinta
        if (tipo == Transferencia.TipoTransferencia.ENTRE_CUENTAS) {
            String origenHash = cuentaOrigen.getNumeroTarjetaHash();
            if (destinoHash.equals(origenHash))
                return "La cuenta destino debe ser diferente a la cuenta origen.";
        }

        // Validación de fondos (ANTES de registrar)
        if (!transferenciaDAO.validarFondos(idCuentaOrigen, monto))
            return "Fondos insuficientes. Saldo disponible insuficiente para esta transferencia.";

        // PATRÓN FACTORY: crear el objeto transferencia del tipo correcto
        Transferencia t = TransferenciaFactory.crear(
                tipo,
                usuarioActual.getIdUsuario(),
                idCuentaOrigen,
                cuentaOrigen.getNumeroTarjetaEnmascarado(),
                destinoHash,
                destinoEnm,
                entidadDestino,
                monto,
                descripcion != null ? descripcion.trim() : "");

        // Persistir en BD (estado PENDIENTE)
        int idTransferencia = transferenciaDAO.registrarTransferencia(
                usuarioActual.getIdUsuario(),
                tipo.getIdTipo(),
                idCuentaOrigen,
                cuentaOrigen.getNumeroTarjetaEnmascarado(),
                destinoHash,
                destinoEnm,
                entidadDestino,
                monto,
                descripcion != null ? descripcion.trim() : "");

        if (idTransferencia <= 0)
            return "Error al registrar la transferencia. Intente nuevamente.";

        t.setIdTransferencia(idTransferencia);
        this.transferenciaActual = t;

        return null;
    }

    // ============================================================
    // PASO 2: Enviar código OTP de confirmación
    // ============================================================

    /**
     * Genera y envía el código OTP para confirmar la transferencia.
     * Retorna null si fue exitoso, o mensaje de error.
     */
    public String enviarCodigoConfirmacion(InicioSesion.CanalVerificacion canal) {

        if (transferenciaActual == null)
            return "No hay una transferencia pendiente de confirmación.";

        codigoGenerado    = SeguridadUtil.generarCodigo6Digitos();
        String codigoHash = SeguridadUtil.hashSHA256(codigoGenerado);

        idSesionTransferencia = transferenciaDAO.crearSesionTransferencia(
                usuarioActual.getIdUsuario(),
                canal.getIdTipo(),
                transferenciaActual.getIdTransferencia(),
                codigoHash);

        if (idSesionTransferencia < 0)
            return "Error al generar el código de confirmación. Intente nuevamente.";

        String destinatario = (canal == InicioSesion.CanalVerificacion.SMS)
                ? usuarioActual.getTelefono()
                : usuarioActual.getEmail();

        // Reutiliza FACTORY + OBSERVER del Módulo 1 para enviar el código
        NotificacionObserver notificador = NotificacionFactory.crear(canal);
        notificador.enviarCodigo(destinatario, codigoGenerado, usuarioActual.getPrimerNombre());

        return null;
    }

    // ============================================================
    // PASO 3: Procesar transferencia (confirmar con OTP)
    // ============================================================

    /**
     * Delega el procesamiento completo a TransferenciaFacade.
     * Retorna null si fue exitoso, o mensaje de error.
     */
    public String procesarTransferencia(String codigoIngresado,
                                        InicioSesion.CanalVerificacion canal) {

        if (transferenciaActual == null)
            return "No hay una transferencia pendiente.";

        String destinatario = (canal == InicioSesion.CanalVerificacion.SMS)
                ? usuarioActual.getTelefono()
                : usuarioActual.getEmail();

        // PATRÓN FACADE: una sola llamada encapsula toda la complejidad
        return facade.procesarTransferencia(
                transferenciaActual,
                codigoIngresado,
                idSesionTransferencia,
                canal,
                destinatario,
                usuarioActual.getPrimerNombre());
    }

    // ============================================================
    // CONSULTAS
    // ============================================================

    /** Lista el historial de transferencias del usuario. */
    public List<Transferencia> obtenerHistorial() {
        if (usuarioActual == null) return List.of();
        return transferenciaDAO.obtenerHistorial(usuarioActual.getIdUsuario());
    }

    /** Obtiene una transferencia por id (para el comprobante). */
    public Transferencia obtenerTransferenciaPorId(int idTransferencia) {
        return transferenciaDAO.obtenerPorId(idTransferencia);
    }

    /** Lista las cuentas vinculadas del usuario para elegir origen. */
    public List<CuentaBancaria> obtenerCuentasVinculadas() {
        if (usuarioActual == null) return List.of();
        return cuentaDAO.obtenerCuentasPorUsuario(usuarioActual.getIdUsuario())
                .stream()
                .filter(CuentaBancaria::estaVinculada)
                .collect(java.util.stream.Collectors.toList());
    }

    /** Lista todas las entidades bancarias disponibles (para interbancaria). */
    public List<TipoCuentaBancaria> obtenerEntidadesBancarias() {
        return cuentaDAO.obtenerTiposCuentaBanco();
    }

    // ============================================================
    // GETTERS DE ESTADO
    // ============================================================

    public Transferencia getTransferenciaActual()       { return transferenciaActual; }
    public String        getCodigoGeneradoSimulacion()  { return codigoGenerado; }

    /** Limpia el estado en memoria (cancelar o tras completar). */
    public void limpiarEstado() {
        transferenciaActual   = null;
        idSesionTransferencia = -1;
        codigoGenerado        = null;
    }
}
