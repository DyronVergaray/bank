package view;

import controller.AutenticacionController;
import model.InicioSesion;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * ============================================================
 * CAPA: VIEW — Vista de Inicio de Sesión
 * ============================================================
 * Pantalla 1 del flujo de autenticación:
 *   - Campos: correo electrónico y contraseña
 *   - Selección de canal de verificación (SMS o Correo)
 *   - Botón "Ingresar" → delega al Controlador
 *   - Botón "Crear cuenta" → abre RegistroView
 *
 * Clase: LoginView
 * Módulo: Gestión de Usuarios y Autenticación — QoriBank
 * ============================================================
 */
public class LoginView extends JFrame {

    // ----- Paleta de colores Qori Bank -----
    static final Color DORADO_PRINCIPAL = new Color(0xF5A800);
    static final Color DORADO_OSCURO    = new Color(0xD48B00);
    static final Color DORADO_CLARO     = new Color(0xFFD966);
    static final Color GRIS_FONDO       = new Color(0xF0F0F0);
    static final Color BLANCO           = Color.WHITE;
    static final Color TEXTO_OSCURO     = new Color(0x2C2C2C);
    static final Color GRIS_CAMPO       = new Color(0xE8E8E8);
    static final Color ROJO_ERROR       = new Color(0xC0392B);

    // ----- Fuentes -----
    static final Font FUENTE_TITULO    = new Font("Segoe UI", Font.BOLD, 22);
    static final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font FUENTE_LABEL     = new Font("Segoe UI", Font.BOLD, 12);
    static final Font FUENTE_CAMPO     = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font FUENTE_BOTON     = new Font("Segoe UI", Font.BOLD, 14);

    // ----- Componentes -----
    private JTextField    txtEmail;
    private JPasswordField txtPassword;
    private JRadioButton  rbSMS;
    private JRadioButton  rbCorreo;
    private JButton       btnIngresar;
    private JLabel        lblError;

    private final AutenticacionController controller;

    public LoginView() {
        this.controller = new AutenticacionController();
        initUI();
    }

    private void initUI() {
        setTitle("Qori Bank — Iniciar Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GRIS_FONDO);
        setContentPane(root);

        root.add(crearBarraLateral(), BorderLayout.WEST);
        root.add(crearPanelLogin(),   BorderLayout.CENTER);
    }

    // --------------------------------------------------------
    // Barra lateral dorada
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

        JLabel lblIcono = new JLabel();
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblIcono.setIcon(crearIconoBanco(90, 90));
        barra.add(lblIcono);

        barra.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel lblNombre = new JLabel("Qori Bank");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblNombre.setForeground(BLANCO);
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.add(lblNombre);

        barra.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel lblSlogan = new JLabel("<html><center>Tu banco digital<br>inclusivo</center></html>");
        lblSlogan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSlogan.setForeground(new Color(255, 255, 255, 200));
        lblSlogan.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.add(lblSlogan);

        barra.add(Box.createVerticalGlue());

        JLabel lblPie = new JLabel("© 2026 Qori Bank");
        lblPie.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPie.setForeground(new Color(255, 255, 255, 150));
        lblPie.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.add(lblPie);

        return barra;
    }

    // --------------------------------------------------------
    // Panel principal de login
    // --------------------------------------------------------
    private JPanel crearPanelLogin() {
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
        card.setPreferredSize(new Dimension(420, 500));
        card.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(6, 0, 6, 0);
        gbc.gridx   = 0;
        gbc.weightx = 1.0;

        // Título
        JLabel lblTitulo = new JLabel("Iniciar Sesión");
        lblTitulo.setFont(FUENTE_TITULO);
        lblTitulo.setForeground(TEXTO_OSCURO);
        gbc.gridy = 0;
        card.add(lblTitulo, gbc);

        JLabel lblSub = new JLabel("Ingrese sus credenciales para continuar");
        lblSub.setFont(FUENTE_SUBTITULO);
        lblSub.setForeground(Color.GRAY);
        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(lblSub, gbc);

        // Email
        gbc.insets = new Insets(4, 0, 2, 0);
        gbc.gridy  = 2;
        card.add(crearLabel("Correo Electrónico"), gbc);
        txtEmail   = crearCampoTexto("correo@ejemplo.com");
        gbc.gridy  = 3;
        card.add(txtEmail, gbc);

        // Contraseña
        gbc.gridy = 4;
        card.add(crearLabel("Contraseña"), gbc);
        txtPassword = new JPasswordField();
        estilizarCampo(txtPassword);
        txtPassword.setFont(FUENTE_CAMPO);
        gbc.gridy = 5;
        card.add(txtPassword, gbc);

        // Canal de verificación
        gbc.gridy  = 6;
        gbc.insets = new Insets(14, 0, 4, 0);
        card.add(crearLabel("Método de verificación en dos pasos:"), gbc);

        rbSMS    = crearRadioButton("SMS");
        rbCorreo = crearRadioButton("Correo electrónico");
        rbSMS.setSelected(true);
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbSMS);
        grupo.add(rbCorreo);

        JPanel panelCanal = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelCanal.setOpaque(false);
        panelCanal.add(rbSMS);
        panelCanal.add(Box.createRigidArea(new Dimension(20, 0)));
        panelCanal.add(rbCorreo);
        gbc.gridy  = 7;
        gbc.insets = new Insets(2, 0, 16, 0);
        card.add(panelCanal, gbc);

        // Error
        lblError = new JLabel("");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(ROJO_ERROR);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy  = 8;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblError, gbc);

        // Botón Ingresar
        btnIngresar = crearBotonPrincipal("Ingresar");
        btnIngresar.addActionListener(e -> procesarLogin());
        gbc.gridy  = 9;
        gbc.insets = new Insets(4, 0, 8, 0);
        card.add(btnIngresar, gbc);

        // Separador visual
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0xDDDDDD));
        gbc.gridy  = 10;
        gbc.insets = new Insets(4, 0, 8, 0);
        card.add(sep, gbc);

        // Botón Crear cuenta
        JButton btnCrearCuenta = new JButton("¿No tienes cuenta? Regístrate aquí");
        btnCrearCuenta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCrearCuenta.setForeground(DORADO_OSCURO);
        btnCrearCuenta.setBorderPainted(false);
        btnCrearCuenta.setContentAreaFilled(false);
        btnCrearCuenta.setFocusPainted(false);
        btnCrearCuenta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCrearCuenta.addActionListener(e -> abrirRegistro());
        gbc.gridy  = 11;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(btnCrearCuenta, gbc);

        // Enter en campos
        KeyAdapter enterListener = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) procesarLogin();
            }
        };
        txtEmail.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);

        panelOuter.add(card);
        return panelOuter;
    }

    // --------------------------------------------------------
    // Evento: Ingresar
    // --------------------------------------------------------
    private void procesarLogin() {
        String email    = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        InicioSesion.CanalVerificacion canal = rbSMS.isSelected()
                ? InicioSesion.CanalVerificacion.SMS
                : InicioSesion.CanalVerificacion.CORREO;

        String errorCredenciales = controller.validarCredenciales(email, password);
        if (errorCredenciales != null) { mostrarError(errorCredenciales); return; }

        String errorCodigo = controller.enviarCodigoVerificacion(canal);
        if (errorCodigo != null) { mostrarError(errorCodigo); return; }

        limpiarError();
        new VerificacionCodigoView(controller, canal,
                controller.getCodigoGeneradoSimulacion()).setVisible(true);
        this.setVisible(false);
    }

    // --------------------------------------------------------
    // Evento: Crear cuenta → abre RegistroView
    // --------------------------------------------------------
    private void abrirRegistro() {
        new RegistroView().setVisible(true);
        this.setVisible(false);
    }

    // --------------------------------------------------------
    // Helpers de UI
    // --------------------------------------------------------
    private void mostrarError(String msg) { lblError.setText(msg); }
    private void limpiarError()           { lblError.setText(""); }

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FUENTE_LABEL);
        lbl.setForeground(TEXTO_OSCURO);
        return lbl;
    }

    private JTextField crearCampoTexto(String placeholder) {
        JTextField campo = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    g2.drawString(placeholder, 20, getHeight() / 2 + 5);
                }
            }
        };
        estilizarCampo(campo);
        return campo;
    }

    private void estilizarCampo(JComponent campo) {
        campo.setPreferredSize(new Dimension(0, 42));
        campo.setFont(FUENTE_CAMPO);
        campo.setBackground(GRIS_CAMPO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCCCCCC), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        campo.setOpaque(true);
    }

    private JRadioButton crearRadioButton(String texto) {
        JRadioButton rb = new JRadioButton(texto);
        rb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rb.setForeground(TEXTO_OSCURO);
        rb.setOpaque(false);
        rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return rb;
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
        btn.setFont(FUENTE_BOTON);
        btn.setPreferredSize(new Dimension(0, 46));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private ImageIcon crearIconoBanco(int ancho, int alto) {
        java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(ancho, alto,
                        java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(255, 255, 255, 60));
        g2.fillOval(0, 0, ancho, alto);
        g2.setColor(new Color(0xFFE066));
        g2.fillOval(ancho / 4, alto / 3, ancho / 2, alto * 2 / 5);
        g2.setColor(new Color(0xF5A800));
        g2.fillArc(ancho / 8, alto / 8, ancho * 3 / 4, alto * 2 / 3, 0, 180);
        g2.setColor(new Color(0x2C2C2C));
        g2.fillOval(ancho * 3 / 8, alto * 5 / 10, ancho / 10, alto / 10);
        g2.fillOval(ancho * 5 / 8 - ancho / 10, alto * 5 / 10, ancho / 10, alto / 10);
        g2.dispose();
        return new ImageIcon(img);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}
