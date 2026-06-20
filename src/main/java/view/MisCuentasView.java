package view;

import controller.AutenticacionController;
import controller.CuentaController;
import model.CuentaBancaria;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * ============================================================
 * CAPA: VIEW — Vista de Mis Cuentas Bancarias
 * ============================================================
 * Pantalla exclusiva para CLIENTE: muestra las tarjetas que ha
 * vinculado (o que están pendientes/con error) y permite
 * registrar una nueva.
 *
 * Clase: MisCuentasView
 * Módulo: Vinculación de Cuentas Bancarias — QoriBank
 * ============================================================
 */
public class MisCuentasView extends JFrame {

    private static final Color DORADO_PRINCIPAL = LoginView.DORADO_PRINCIPAL;
    private static final Color DORADO_OSCURO    = LoginView.DORADO_OSCURO;
    private static final Color GRIS_FONDO       = LoginView.GRIS_FONDO;
    private static final Color BLANCO           = LoginView.BLANCO;
    private static final Color TEXTO_OSCURO     = LoginView.TEXTO_OSCURO;
    private static final Color VERDE_ACTIVO     = new Color(0x27AE60);
    private static final Color AMARILLO_PEND    = new Color(0xE0A100);
    private static final Color ROJO_ERROR       = new Color(0xC0392B);
    private static final Color MORADO_CUENTAS   = new Color(0x6C5CE7);

    private final AutenticacionController authController;
    private final CuentaController        cuentaController;

    private JPanel listaPanel;

    public MisCuentasView(AutenticacionController authController) {
        this.authController  = authController;
        this.cuentaController = new CuentaController(authController.getUsuarioActual());
        initUI();
        cargarCuentas();
    }

    private void initUI() {
        setTitle("Qori Bank — Mis Cuentas Bancarias");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GRIS_FONDO);
        setContentPane(root);

        root.add(crearBarraSuperior(), BorderLayout.NORTH);
        root.add(crearPanelCentral(),  BorderLayout.CENTER);
    }

    // --------------------------------------------------------
    // Barra superior
    // --------------------------------------------------------
    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint grad = new GradientPaint(
                        0, 0, DORADO_PRINCIPAL, getWidth(), 0, DORADO_OSCURO);
                g2.setPaint(grad);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        barra.setPreferredSize(new Dimension(0, 70));
        barra.setMinimumSize(new Dimension(0, 70));
        barra.setLayout(new BorderLayout());
        barra.setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        izq.setOpaque(false);
        JLabel lblIcono = new JLabel("\uD83D\uDCB3");
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        JLabel lblTitulo = new JLabel("Mis Cuentas Bancarias");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(BLANCO);
        izq.add(lblIcono);
        izq.add(lblTitulo);
        barra.add(izq, BorderLayout.CENTER);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        der.setOpaque(false);

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
    // Panel central
    // --------------------------------------------------------
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(GRIS_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Header con botón "Vincular nueva cuenta"
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblSub = new JLabel("Tarjetas vinculadas a tu cuenta QoriBank");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(Color.GRAY);
        header.add(lblSub, BorderLayout.WEST);

        JButton btnNueva = crearBotonAccion("+ Vincular nueva cuenta", MORADO_CUENTAS);
        btnNueva.addActionListener(e -> abrirRegistroCuenta());
        header.add(btnNueva, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        // Lista de cuentas (scroll)
        listaPanel = new JPanel();
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS));
        listaPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(listaPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(GRIS_FONDO);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // --------------------------------------------------------
    // Carga y renderiza las cuentas del usuario
    // --------------------------------------------------------
    private void cargarCuentas() {
        listaPanel.removeAll();

        List<CuentaBancaria> cuentas = cuentaController.obtenerCuentasDelUsuario();

        if (cuentas.isEmpty()) {
            JLabel lblVacio = new JLabel("Aún no tienes cuentas bancarias vinculadas.");
            lblVacio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblVacio.setForeground(Color.GRAY);
            lblVacio.setAlignmentX(Component.LEFT_ALIGNMENT);
            listaPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            listaPanel.add(lblVacio);
        } else {
            for (CuentaBancaria c : cuentas) {
                listaPanel.add(crearTarjetaCuenta(c));
                listaPanel.add(Box.createRigidArea(new Dimension(0, 12)));
            }
        }

        listaPanel.revalidate();
        listaPanel.repaint();
    }

    // --------------------------------------------------------
    // Tarjeta visual de una cuenta bancaria
    // --------------------------------------------------------
    private JPanel crearTarjetaCuenta(CuentaBancaria c) {
        JPanel card = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLANCO);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.setPreferredSize(new Dimension(700, 110));

        // Izquierda: entidad + número enmascarado + saldo
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblEntidad = new JLabel(c.getNombreEntidad());
        lblEntidad.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblEntidad.setForeground(TEXTO_OSCURO);

        JLabel lblNumero = new JLabel(c.getNumeroTarjetaEnmascarado()
                + "   ·   Vence " + c.getFechaVencimiento());
        lblNumero.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblNumero.setForeground(Color.GRAY);

        JLabel lblSaldo = new JLabel("Saldo: S/ " + String.format("%,.2f", c.getSaldo()));
        lblSaldo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSaldo.setForeground(new Color(0x27AE60));

        info.add(lblEntidad);
        info.add(Box.createRigidArea(new Dimension(0, 4)));
        info.add(lblNumero);
        info.add(Box.createRigidArea(new Dimension(0, 4)));
        info.add(lblSaldo);
        card.add(info, BorderLayout.WEST);

        // Derecha: badge de estado
        card.add(crearBadgeEstado(c.getEstado()), BorderLayout.EAST);

        return card;
    }

    private JLabel crearBadgeEstado(CuentaBancaria.EstadoCuenta estado) {
        Color color;
        String texto;
        switch (estado) {
            case VINCULADA:
                color = VERDE_ACTIVO;
                texto = "VINCULADA";
                break;
            case ERROR:
                color = ROJO_ERROR;
                texto = "ERROR";
                break;
            default:
                color = AMARILLO_PEND;
                texto = "PENDIENTE";
        }

        JLabel badge = new JLabel("  " + texto + "  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(BLANCO);
        badge.setOpaque(false);
        return badge;
    }

    // --------------------------------------------------------
    // Evento: abrir registro de nueva cuenta
    // --------------------------------------------------------
    private void abrirRegistroCuenta() {
        RegistrarCuentaView vista = new RegistrarCuentaView(authController, this);
        vista.setVisible(true);
        this.setVisible(false);
    }

    /** Llamado por RegistrarCuentaView al volver, para refrescar la lista. */
    public void refrescarAlVolver() {
        cargarCuentas();
        this.setVisible(true);
    }

    // --------------------------------------------------------
    // Helpers de UI
    // --------------------------------------------------------
    private JButton crearBotonBarra(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(DORADO_OSCURO);
        btn.setBackground(BLANCO);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DORADO_OSCURO, 1, true),
                BorderFactory.createEmptyBorder(5, 14, 5, 14)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    private JButton crearBotonAccion(String texto, Color color) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? color.brighter() : color);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(BLANCO);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(200, 38));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
