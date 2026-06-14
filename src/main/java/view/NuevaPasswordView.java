package view;

import controller.AutenticacionController;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * ============================================================
 * CAPA: VIEW — Vista de Nueva Contraseña
 * ============================================================
 * Pantalla final del flujo de recuperación de contraseña:
 *   - El usuario ya verificó su identidad y el código OTP
 *   - Ingresa y confirma su nueva contraseña
 *   - Botón "Cambiar Contraseña" → delega al controlador
 *   - Al finalizar, vuelve al inicio de sesión
 *
 * Clase: NuevaPasswordView
 * Módulo: Gestión de Usuarios y Autenticación — QoriBank
 * ============================================================
 */
public class NuevaPasswordView extends JFrame {

    private static final Color DORADO_PRINCIPAL = LoginView.DORADO_PRINCIPAL;
    private static final Color DORADO_OSCURO    = LoginView.DORADO_OSCURO;
    private static final Color DORADO_CLARO     = LoginView.DORADO_CLARO;
    private static final Color GRIS_FONDO       = LoginView.GRIS_FONDO;
    private static final Color BLANCO           = LoginView.BLANCO;
    private static final Color TEXTO_OSCURO     = LoginView.TEXTO_OSCURO;
    private static final Color GRIS_CAMPO       = LoginView.GRIS_CAMPO;
    private static final Color ROJO_ERROR       = LoginView.ROJO_ERROR;
    private static final Color VERDE_EXITO      = new Color(0x27AE60);

    private JPasswordField txtPassword;
    private JPasswordField txtConfirm;
    private JLabel         lblError;
    private JLabel         lblExito;

    private final AutenticacionController controller;

    public NuevaPasswordView(AutenticacionController controller) {
        this.controller = controller;
        initUI();
    }

    private void initUI() {
        setTitle("Qori Bank — Nueva Contraseña");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GRIS_FONDO);
        setContentPane(root);

        root.add(crearBarraLateral(), BorderLayout.WEST);
        root.add(crearPanelNuevaPassword(), BorderLayout.CENTER);
    }

    // --------------------------------------------------------
    // Barra lateral
    // --------------------------------------------------------
    private JPanel crearBarraLateral() {
        JPanel barra = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint grad = new GradientPaint(
                        0, 0, DORADO_PRINCIPAL, 0, getHeight(), DORADO_OSCURO);
                g2.setPaint(grad);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        barra.setPreferredSize(new Dimension(260, 0));
        barra.setLayout(new BoxLayout(barra, BoxLayout.Y_AXIS));
        barra.setBorder(BorderFactory.createEmptyBorder(50, 30, 30, 30));

        JLabel lblIcono = new JLabel("🔓") {
            { setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60)); }
        };
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.add(lblIcono);

        barra.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel lblTitulo = new JLabel("Nueva Contraseña");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(BLANCO);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.add(lblTitulo);

        barra.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel lblDesc = new JLabel(
                "<html><center>Identidad verificada.<br>Establezca su nueva contraseña.</center></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(new Color(255, 255, 255, 200));
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.add(lblDesc);

        barra.add(Box.createVerticalGlue());

        // Pasos visuales del flujo
        JLabel lblPaso1 = etiquetaPaso("✅  Datos verificados");
        JLabel lblPaso2 = etiquetaPaso("✅  Código verificado");
        JLabel lblPaso3 = etiquetaPaso("▶  Nueva contraseña");
        barra.add(lblPaso1);
        barra.add(Box.createRigidArea(new Dimension(0, 8)));
        barra.add(lblPaso2);
        barra.add(Box.createRigidArea(new Dimension(0, 8)));
        barra.add(lblPaso3);
        barra.add(Box.createRigidArea(new Dimension(0, 20)));

        return barra;
    }

    // --------------------------------------------------------
    // Panel principal
    // --------------------------------------------------------
    private JPanel crearPanelNuevaPassword() {
        JPanel panelOuter = new JPanel(new GridBagLayout());
        panelOuter.setBackground(GRIS_FONDO);

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLANCO);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        card.setLayout(new GridBagLayout());
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(420, 400));
        card.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.gridx   = 0;
        gbc.weightx = 1.0;

        // Título
        JLabel lblTitulo = new JLabel("Establecer Nueva Contraseña");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(TEXTO_OSCURO);
        gbc.gridy  = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblTitulo, gbc);

        JLabel lblSub = new JLabel(
                "<html>Ingrese su nueva contraseña. Debe tener<br>al menos 6 caracteres.</html>");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(Color.GRAY);
        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(lblSub, gbc);

        // Nueva contraseña
        JLabel lblPwd = new JLabel("Nueva Contraseña");
        lblPwd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPwd.setForeground(TEXTO_OSCURO);
        gbc.gridy  = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblPwd, gbc);

        txtPassword = new JPasswordField();
        estilizarCampo(txtPassword);
        gbc.gridy  = 3;
        gbc.insets = new Insets(0, 0, 12, 0);
        card.add(txtPassword, gbc);

        // Confirmar contraseña
        JLabel lblConfirm = new JLabel("Confirmar Contraseña");
        lblConfirm.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblConfirm.setForeground(TEXTO_OSCURO);
        gbc.gridy  = 4;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblConfirm, gbc);

        txtConfirm = new JPasswordField();
        estilizarCampo(txtConfirm);
        gbc.gridy  = 5;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(txtConfirm, gbc);

        // Mensajes
        lblError = new JLabel("");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(ROJO_ERROR);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy  = 6;
        gbc.insets = new Insets(0, 0, 2, 0);
        card.add(lblError, gbc);

        lblExito = new JLabel("");
        lblExito.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblExito.setForeground(VERDE_EXITO);
        lblExito.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy  = 7;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblExito, gbc);

        // Botón Cambiar Contraseña
        JButton btnCambiar = crearBotonPrincipal("Cambiar Contraseña");
        btnCambiar.addActionListener(e -> procesarCambio());
        gbc.gridy  = 8;
        gbc.insets = new Insets(4, 0, 0, 0);
        card.add(btnCambiar, gbc);

        // Enter en campos
        KeyAdapter enterListener = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) procesarCambio();
            }
        };
        txtPassword.addKeyListener(enterListener);
        txtConfirm.addKeyListener(enterListener);

        panelOuter.add(card);
        return panelOuter;
    }

    // --------------------------------------------------------
    // Evento: Cambiar Contraseña
    // --------------------------------------------------------
    private void procesarCambio() {
        lblError.setText("");
        lblExito.setText("");

        String nueva     = new String(txtPassword.getPassword());
        String confirmar = new String(txtConfirm.getPassword());

        String error = controller.cambiarPassword(nueva, confirmar);

        if (error != null) {
            lblError.setText(error);
            txtPassword.setText("");
            txtConfirm.setText("");
            txtPassword.requestFocus();
            return;
        }

        lblExito.setText("✅ ¡Contraseña actualizada! Redirigiendo al inicio de sesión...");
        txtPassword.setText("");
        txtConfirm.setText("");

        Timer timer = new Timer(2000, e -> {
            controller.cerrarSesion();
            new LoginView().setVisible(true);
            this.dispose();
        });
        timer.setRepeats(false);
        timer.start();
    }

    // --------------------------------------------------------
    // Helpers de UI
    // --------------------------------------------------------
    private void estilizarCampo(JComponent campo) {
        campo.setPreferredSize(new Dimension(0, 42));
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBackground(GRIS_CAMPO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCCCCCC), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        campo.setOpaque(true);
    }

    private JButton crearBotonPrincipal(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? DORADO_OSCURO
                        : getModel().isRollover() ? DORADO_CLARO
                        : DORADO_PRINCIPAL);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(TEXTO_OSCURO);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(0, 46));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel etiquetaPaso(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(BLANCO);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }
}
