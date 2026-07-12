package view;

import controller.AutenticacionController;
import controller.NotificacionController;
import model.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ============================================================
 * CAPA: VIEW — Vista de Notificaciones y Alertas
 * ============================================================
 * Pantalla exclusiva para CLIENTE. Permite:
 *   - Elegir el canal de notificaciones (SMS / Correo)
 *   - Ver todas las notificaciones con fecha/hora, tipo y canal
 *   - Confirmar o cancelar alertas de seguridad (>= S/500)
 *   - Marcar notificaciones como leídas
 *
 * Clase: NotificacionesView
 * Módulo: Notificaciones y Alertas — QoriBank
 * ============================================================
 */
public class NotificacionesView extends JFrame {

    private static final Color DORADO_PRINCIPAL = LoginView.DORADO_PRINCIPAL;
    private static final Color DORADO_OSCURO    = LoginView.DORADO_OSCURO;
    private static final Color GRIS_FONDO       = LoginView.GRIS_FONDO;
    private static final Color BLANCO           = LoginView.BLANCO;
    private static final Color TEXTO_OSCURO     = LoginView.TEXTO_OSCURO;
    private static final Color GRIS_CAMPO       = LoginView.GRIS_CAMPO;
    private static final Color NARANJA          = new Color(0xE67E22);
    private static final Color NARANJA_OSCURO   = new Color(0xCA6F1E);
    private static final Color VERDE_EXITO      = new Color(0x27AE60);
    private static final Color ROJO_ERROR       = new Color(0xC0392B);
    private static final Color AZUL_INFO        = new Color(0x2980B9);
    private static final Color AMARILLO_ALERTA  = new Color(0xF39C12);

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AutenticacionController  authController;
    private final NotificacionController   notifController;

    private JRadioButton rbSMS;
    private JRadioButton rbCorreo;
    private JPanel       panelLista;
    private JLabel       lblContador;

    public NotificacionesView(AutenticacionController authController) {
        this.authController  = authController;
        this.notifController = new NotificacionController(authController.getUsuarioActual());
        initUI();
        cargarNotificaciones();
    }

    private void initUI() {
        setTitle("Qori Bank — Notificaciones");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 680);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GRIS_FONDO);
        setContentPane(root);

        root.add(crearBarraSuperior(), BorderLayout.NORTH);
        root.add(crearPanelPreferencia(), BorderLayout.WEST);
        root.add(crearPanelLista(),       BorderLayout.CENTER);
    }

    // --------------------------------------------------------
    // Barra superior
    // --------------------------------------------------------
    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0,0,NARANJA,getWidth(),0,NARANJA_OSCURO));
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        barra.setPreferredSize(new Dimension(0, 70));
        barra.setLayout(new BorderLayout());
        barra.setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        izq.setOpaque(false);
        JLabel lblTitulo = new JLabel("Notificaciones y Alertas");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(BLANCO);
        lblContador = new JLabel("");
        lblContador.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblContador.setForeground(new Color(255,255,255,200));
        izq.add(lblTitulo);
        izq.add(lblContador);
        barra.add(izq, BorderLayout.CENTER);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        der.setOpaque(false);

        JButton btnMarcarTodas = crearBotonBarra("Marcar todas leídas");
        btnMarcarTodas.addActionListener(e -> {
            notifController.marcarTodasLeidas();
            cargarNotificaciones();
        });
        der.add(btnMarcarTodas);

        JButton btnActualizar = crearBotonBarra("⟳ Actualizar");
        btnActualizar.addActionListener(e -> cargarNotificaciones());
        der.add(btnActualizar);

        JButton btnVolver = crearBotonBarra("← Volver");
        btnVolver.addActionListener(e -> {
            new BienvenidaView(authController).setVisible(true);
            this.dispose();
        });
        der.add(btnVolver);
        barra.add(der, BorderLayout.EAST);
        return barra;
    }

    // --------------------------------------------------------
    // Panel izquierdo: preferencia de canal
    // --------------------------------------------------------
    private JPanel crearPanelPreferencia() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLANCO);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(240, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));
        panel.setOpaque(true);
        panel.setBackground(BLANCO);

        JLabel lblPregunta = new JLabel(
                "<html><b>¿A dónde desea que se envíen<br>sus notificaciones?</b></html>");
        lblPregunta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPregunta.setForeground(TEXTO_OSCURO);
        lblPregunta.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblPregunta);
        panel.add(Box.createRigidArea(new Dimension(0, 16)));

        rbSMS    = new JRadioButton("SMS");
        rbCorreo = new JRadioButton("Correo electrónico");

        rbSMS.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rbCorreo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rbSMS.setForeground(TEXTO_OSCURO);
        rbCorreo.setForeground(TEXTO_OSCURO);
        rbSMS.setOpaque(false);
        rbCorreo.setOpaque(false);
        rbSMS.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rbCorreo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbSMS);
        grupo.add(rbCorreo);

        // Cargar preferencia guardada
        String canalActual = notifController.obtenerCanalPreferido();
        if ("SMS".equalsIgnoreCase(canalActual)) rbSMS.setSelected(true);
        else rbCorreo.setSelected(true);

        // Guardar al cambiar
        rbSMS.addActionListener(e -> notifController.guardarPreferencia("SMS"));
        rbCorreo.addActionListener(e -> notifController.guardarPreferencia("CORREO"));

        rbSMS.setAlignmentX(Component.LEFT_ALIGNMENT);
        rbCorreo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(rbSMS);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(rbCorreo);

        panel.add(Box.createRigidArea(new Dimension(0, 24)));

        // Separador
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(0xDDDDDD));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sep);

        panel.add(Box.createRigidArea(new Dimension(0, 16)));

        // Leyenda de tipos
        JLabel lblLeyenda = new JLabel("<html><b>Tipos de notificación:</b></html>");
        lblLeyenda.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLeyenda.setForeground(TEXTO_OSCURO);
        lblLeyenda.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblLeyenda);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(crearLeyenda("SESIÓN", AZUL_INFO));
        panel.add(Box.createRigidArea(new Dimension(0, 6)));
        panel.add(crearLeyenda("TRANSFERENCIA", VERDE_EXITO));
        panel.add(Box.createRigidArea(new Dimension(0, 6)));
        panel.add(crearLeyenda("ALERTA", AMARILLO_ALERTA));

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    // --------------------------------------------------------
    // Panel central: lista de notificaciones
    // --------------------------------------------------------
    private JPanel crearPanelLista() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(GRIS_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 24));

        panelLista = new JPanel();
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));
        panelLista.setOpaque(false);

        JScrollPane scroll = new JScrollPane(panelLista);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(GRIS_FONDO);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // --------------------------------------------------------
    // Carga y renderiza las notificaciones
    // --------------------------------------------------------
    private void cargarNotificaciones() {
        panelLista.removeAll();

        List<Notificacion> lista = notifController.obtenerNotificaciones();
        int noLeidas = notifController.contarNoLeidas();

        lblContador.setText(noLeidas > 0
                ? "  (" + noLeidas + " sin leer)"
                : "  (todas leídas)");

        if (lista.isEmpty()) {
            JLabel lblVacio = new JLabel("No tienes notificaciones aún.");
            lblVacio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblVacio.setForeground(Color.GRAY);
            lblVacio.setAlignmentX(Component.LEFT_ALIGNMENT);
            panelLista.add(Box.createRigidArea(new Dimension(0, 20)));
            panelLista.add(lblVacio);
        } else {
            for (Notificacion n : lista) {
                panelLista.add(crearTarjetaNotificacion(n));
                panelLista.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        panelLista.revalidate();
        panelLista.repaint();
    }

    // --------------------------------------------------------
    // Tarjeta visual de una notificación
    // --------------------------------------------------------
    private JPanel crearTarjetaNotificacion(Notificacion n) {
        Color colorTipo = colorDeTipo(n.getTipo());
        boolean esAlerta = n.getTipo() == Notificacion.TipoNotificacion.ALERTA
                && !n.isLeida()
                && n.getIdTransferencia() != null;

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // Fondo más claro si ya está leída
                g2.setColor(n.isLeida() ? new Color(0xFAFAFA) : BLANCO);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                // Barra lateral de color según tipo
                g2.setColor(colorTipo);
                g2.fill(new RoundRectangle2D.Float(0, 0, 5, getHeight(), 4, 4));
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(12, 0));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, esAlerta ? 130 : 90));
        card.setPreferredSize(new Dimension(500, esAlerta ? 130 : 90));

        // Panel izquierdo: badge de tipo + mensaje
        JPanel izq = new JPanel();
        izq.setLayout(new BoxLayout(izq, BoxLayout.Y_AXIS));
        izq.setOpaque(false);

        // Fila superior: badge tipo + canal + estado leída
        JPanel filaTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaTop.setOpaque(false);
        filaTop.add(crearBadge(n.getTipo().name(), colorTipo));
        filaTop.add(crearBadge(n.getCanal(), new Color(0x7F8C8D)));
        if (n.isLeida()) {
            JLabel lblLeida = new JLabel("leída");
            lblLeida.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblLeida.setForeground(Color.LIGHT_GRAY);
            filaTop.add(lblLeida);
        }
        filaTop.setAlignmentX(Component.LEFT_ALIGNMENT);
        izq.add(filaTop);
        izq.add(Box.createRigidArea(new Dimension(0, 6)));

        // Mensaje
        JLabel lblMensaje = new JLabel(
                "<html><body style='width:400px'>" + n.getMensaje() + "</body></html>");
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN,
                n.isLeida() ? 12 : 13));
        lblMensaje.setForeground(n.isLeida() ? Color.GRAY : TEXTO_OSCURO);
        lblMensaje.setAlignmentX(Component.LEFT_ALIGNMENT);
        izq.add(lblMensaje);

        izq.add(Box.createRigidArea(new Dimension(0, 4)));

        // Fecha y hora
        JLabel lblFecha = new JLabel(n.getFechaHora() != null
                ? n.getFechaHora().format(FMT) : "-");
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFecha.setForeground(Color.GRAY);
        lblFecha.setAlignmentX(Component.LEFT_ALIGNMENT);
        izq.add(lblFecha);

        // Botones de alerta (solo para ALERTA no leída con transferencia)
        if (esAlerta) {
            izq.add(Box.createRigidArea(new Dimension(0, 8)));
            JPanel filaBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            filaBotones.setOpaque(false);

            JButton btnConfirmar = crearBotonAlerta("Confirmar transferencia", VERDE_EXITO);
            btnConfirmar.addActionListener(e -> abrirConfirmacionAlerta(n));

            JButton btnCancelar = crearBotonAlerta("Cancelar transferencia", ROJO_ERROR);
            btnCancelar.addActionListener(e -> cancelarAlerta(n));

            filaBotones.add(btnConfirmar);
            filaBotones.add(btnCancelar);
            filaBotones.setAlignmentX(Component.LEFT_ALIGNMENT);
            izq.add(filaBotones);
        }

        card.add(izq, BorderLayout.CENTER);

        // Botón marcar leída (solo si no está leída y no es alerta)
        if (!n.isLeida() && !esAlerta) {
            JButton btnLeida = new JButton("✓");
            btnLeida.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnLeida.setForeground(colorTipo);
            btnLeida.setBorderPainted(false);
            btnLeida.setContentAreaFilled(false);
            btnLeida.setFocusPainted(false);
            btnLeida.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnLeida.setToolTipText("Marcar como leída");
            btnLeida.addActionListener(e -> {
                notifController.marcarLeida(n.getIdNotificacion());
                cargarNotificaciones();
            });
            JPanel der = new JPanel(new GridBagLayout());
            der.setOpaque(false);
            der.add(btnLeida);
            card.add(der, BorderLayout.EAST);
        }

        return card;
    }

    // --------------------------------------------------------
    // Confirmar alerta: abre diálogo de código OTP
    // --------------------------------------------------------
    private void abrirConfirmacionAlerta(Notificacion alerta) {
        InicioSesion.CanalVerificacion canal = rbSMS.isSelected()
                ? InicioSesion.CanalVerificacion.SMS
                : InicioSesion.CanalVerificacion.CORREO;

        String errorEnvio = notifController.enviarCodigoAlerta(alerta, canal);
        if (errorEnvio != null) {
            JOptionPane.showMessageDialog(this, errorEnvio, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Diálogo de ingreso de código
        JPanel panelCodigo = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.gridx = 0; g.weightx = 1.0;

        JLabel lblInfo = new JLabel("<html>Se envió un código de confirmación por "
                + canal.getNombre() + ".<br>"
                + "<b>Simulación: " + notifController.getCodigoGeneradoSimulacion() + "</b></html>");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.gridy = 0; g.insets = new Insets(0,0,12,0);
        panelCodigo.add(lblInfo, g);

        JTextField txtCodigo = new JTextField();
        txtCodigo.setPreferredSize(new Dimension(200, 38));
        txtCodigo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtCodigo.setHorizontalAlignment(JTextField.CENTER);
        g.gridy = 1; g.insets = new Insets(0,0,0,0);
        panelCodigo.add(txtCodigo, g);

        int resultado = JOptionPane.showConfirmDialog(this, panelCodigo,
                "Confirmar transferencia de alto monto",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado != JOptionPane.OK_OPTION) return;

        String errorConfirm = notifController.confirmarAlerta(txtCodigo.getText(), canal);
        if (errorConfirm != null) {
            JOptionPane.showMessageDialog(this, errorConfirm, "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "¡Transferencia confirmada y procesada exitosamente!",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarNotificaciones();
        }
    }

    // --------------------------------------------------------
    // Cancelar alerta
    // --------------------------------------------------------
    private void cancelarAlerta(Notificacion alerta) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea cancelar esta transferencia?",
                "Cancelar transferencia",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        String error = notifController.cancelarAlerta(alerta.getIdTransferencia());
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Transferencia cancelada correctamente.",
                    "Cancelada", JOptionPane.INFORMATION_MESSAGE);
            cargarNotificaciones();
        }
    }

    // --------------------------------------------------------
    // Helpers de UI
    // --------------------------------------------------------
    private Color colorDeTipo(Notificacion.TipoNotificacion tipo) {
        switch (tipo) {
            case SESION:        return AZUL_INFO;
            case TRANSFERENCIA: return VERDE_EXITO;
            case ALERTA:        return AMARILLO_ALERTA;
            default:            return Color.GRAY;
        }
    }

    private JLabel crearBadge(String texto, Color color) {
        JLabel badge = new JLabel("  " + texto + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(BLANCO);
        badge.setOpaque(false);
        return badge;
    }

    private JLabel crearLeyenda(String texto, Color color) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JLabel badge = crearBadge(texto, color);
        fila.add(badge);
        // Retornamos un JLabel envolviendo el panel (truco para BoxLayout)
        JLabel lbl = new JLabel(" " + texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(color);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton crearBotonAlerta(String texto, Color color) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? color.brighter() : color);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8));
                g2.setColor(BLANCO);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setPreferredSize(new Dimension(0, 30));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton crearBotonBarra(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(NARANJA);
        btn.setBackground(BLANCO);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BLANCO, 1, true),
                BorderFactory.createEmptyBorder(5, 14, 5, 14)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }
}
