package view;

import controller.AutenticacionController;
import model.InicioSesion;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * ============================================================
 * CAPA: VIEW — Vista de Recuperación de Contraseña
 * ============================================================
 * Pantalla 1 del flujo de recuperación de contraseña:
 *   - El usuario ingresa: correo, primer nombre, apellido paterno
 *     y teléfono (datos que deben coincidir con su registro)
 *   - Selecciona canal de verificación (SMS o Correo)
 *   - Botón "Continuar" → valida datos y envía código OTP
 *
 * Si los datos son correctos, reutiliza VerificacionCodigoView
 * (en modo recuperación) para validar el código, y luego
 * abre NuevaPasswordView para establecer la nueva contraseña.
 *
 * Clase: RecuperarPasswordView
 * Módulo: Gestión de Usuarios y Autenticación — QoriBank
 * ============================================================
 */
public class RecuperarPasswordView extends JFrame {

    private static final Color DORADO_PRINCIPAL = LoginView.DORADO_PRINCIPAL;
    private static final Color DORADO_OSCURO    = LoginView.DORADO_OSCURO;
    private static final Color DORADO_CLARO     = LoginView.DORADO_CLARO;
    private static final Color GRIS_FONDO       = LoginView.GRIS_FONDO;
    private static final Color BLANCO           = LoginView.BLANCO;
    private static final Color TEXTO_OSCURO     = LoginView.TEXTO_OSCURO;
    private static final Color GRIS_CAMPO       = LoginView.GRIS_CAMPO;
    private static final Color ROJO_ERROR       = LoginView.ROJO_ERROR;

    private JTextField   txtEmail;
    private JTextField   txtNombre;
    private JTextField   txtApellidoP;
    private JTextField   txtTelefono;
    private JRadioButton rbSMS;
    private JRadioButton rbCorreo;
    private JLabel       lblError;

    private final AutenticacionController controller;

    public RecuperarPasswordView() {
        this.controller = new AutenticacionController();
        initUI();
    }

    private void initUI() {
        setTitle("Qori Bank — Recuperar Contraseña");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GRIS_FONDO);
        setContentPane(root);

        root.add(crearBarraLateral(), BorderLayout.WEST);
        root.add(crearPanelRecuperacion(), BorderLayout.CENTER);
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
        barra.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Glue superior: centra verticalmente el bloque ícono/título/descripción
        barra.add(Box.createVerticalGlue());

        JLabel lblIcono = new JLabel("\u26BF") {
            { setFont(new Font("Segoe UI Symbol", Font.PLAIN, 60)); }
        };
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.add(lblIcono);

        barra.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel lblTitulo = new JLabel("Recuperar Acceso");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(BLANCO);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        barra.add(lblTitulo);

        barra.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel lblDesc = new JLabel(
                "<html><center>Verifique su identidad<br>para restablecer su contraseña</center></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(new Color(255, 255, 255, 200));
        lblDesc.setHorizontalAlignment(SwingConstants.CENTER);
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        lblDesc.setPreferredSize(new Dimension(200, 40));
        barra.add(lblDesc);

        // Glue inferior: separa el bloque superior de los pasos
        barra.add(Box.createVerticalGlue());

        // Pasos visuales del flujo
        JLabel lblPaso1 = etiquetaPaso("Verificar identidad");
        JLabel lblPaso2 = etiquetaPasoGris("Ingresar código");
        JLabel lblPaso3 = etiquetaPasoGris("Nueva contraseña");
        barra.add(lblPaso1);
        barra.add(Box.createRigidArea(new Dimension(0, 8)));
        barra.add(lblPaso2);
        barra.add(Box.createRigidArea(new Dimension(0, 8)));
        barra.add(lblPaso3);
        barra.add(Box.createRigidArea(new Dimension(0, 20)));

        return barra;
    }

    // --------------------------------------------------------
    // Panel principal de recuperación
    // --------------------------------------------------------
    private JPanel crearPanelRecuperacion() {
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
        card.setPreferredSize(new Dimension(440, 550));
        card.setBorder(BorderFactory.createEmptyBorder(28, 40, 28, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.gridx   = 0;
        gbc.weightx = 1.0;

        // Título
        JLabel lblTitulo = new JLabel("Recuperar Contraseña");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(TEXTO_OSCURO);
        gbc.gridy  = 0;
        gbc.insets = new Insets(0, 0, 2, 0);
        card.add(lblTitulo, gbc);

        JLabel lblSub = new JLabel(
                "<html>Ingrese sus datos registrados para verificar<br>su identidad.</html>");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(Color.GRAY);
        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(lblSub, gbc);

        // Correo
        gbc.gridy  = 2;
        gbc.insets = new Insets(4, 0, 2, 0);
        card.add(crearLabel("Correo Electrónico"), gbc);
        txtEmail = crearCampo("correo@ejemplo.com");
        gbc.gridy  = 3;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(txtEmail, gbc);

        // Primer nombre
        gbc.gridy  = 4;
        gbc.insets = new Insets(0, 0, 2, 0);
        card.add(crearLabel("Primer Nombre"), gbc);
        txtNombre = crearCampo("Ej: Carlos");
        gbc.gridy  = 5;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(txtNombre, gbc);

        // Apellido paterno
        gbc.gridy  = 6;
        gbc.insets = new Insets(0, 0, 2, 0);
        card.add(crearLabel("Apellido Paterno"), gbc);
        txtApellidoP = crearCampo("Ej: Quispe");
        gbc.gridy  = 7;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(txtApellidoP, gbc);

        // Teléfono
        gbc.gridy  = 8;
        gbc.insets = new Insets(0, 0, 2, 0);
        card.add(crearLabel("Teléfono (9 dígitos, sin +51)"), gbc);
        txtTelefono = crearCampo("Ej: 987654321");
        aplicarFiltroTelefono(txtTelefono);
        gbc.gridy  = 9;
        gbc.insets = new Insets(0, 0, 12, 0);
        card.add(txtTelefono, gbc);

        // Canal de verificación
        gbc.gridy  = 10;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(crearLabel("Enviar código de verificación por:"), gbc);

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
        gbc.gridy  = 11;
        gbc.insets = new Insets(2, 0, 14, 0);
        card.add(panelCanal, gbc);

        // Error
        lblError = new JLabel("");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(ROJO_ERROR);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy  = 12;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblError, gbc);

        // Botón Continuar
        JButton btnContinuar = crearBotonPrincipal("Continuar");
        btnContinuar.addActionListener(e -> procesarRecuperacion());
        gbc.gridy  = 13;
        gbc.insets = new Insets(4, 0, 8, 0);
        card.add(btnContinuar, gbc);

        // Separador
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0xDDDDDD));
        gbc.gridy  = 14;
        gbc.insets = new Insets(4, 0, 8, 0);
        card.add(sep, gbc);

        // Volver al login
        JButton btnVolver = new JButton("← Volver al inicio de sesión");
        btnVolver.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnVolver.setForeground(DORADO_OSCURO);
        btnVolver.setBorderPainted(false);
        btnVolver.setContentAreaFilled(false);
        btnVolver.setFocusPainted(false);
        btnVolver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> {
            new LoginView().setVisible(true);
            this.dispose();
        });
        gbc.gridy  = 15;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(btnVolver, gbc);

        panelOuter.add(card);
        return panelOuter;
    }

    // --------------------------------------------------------
    // Evento: Continuar
    // --------------------------------------------------------
    private void procesarRecuperacion() {
        lblError.setText("");

        InicioSesion.CanalVerificacion canal = rbSMS.isSelected()
                ? InicioSesion.CanalVerificacion.SMS
                : InicioSesion.CanalVerificacion.CORREO;

        // PASO 1: validar identidad (sin contraseña)
        String errorDatos = controller.validarDatosRecuperacion(
                txtEmail.getText(),
                txtNombre.getText(),
                txtApellidoP.getText(),
                txtTelefono.getText()
        );

        if (errorDatos != null) {
            lblError.setText(errorDatos);
            return;
        }

        // PASO 2: enviar código OTP (reutiliza el mismo flujo que el login)
        String errorCodigo = controller.enviarCodigoVerificacion(canal);
        if (errorCodigo != null) {
            lblError.setText(errorCodigo);
            return;
        }

        // Abrir verificación de código en modo recuperación
        new VerificacionCodigoView(controller, canal,
                controller.getCodigoGeneradoSimulacion(), true).setVisible(true);
        this.dispose();
    }

    // --------------------------------------------------------
    // Helpers de UI
    // --------------------------------------------------------

    /**
     * Restringe un campo de texto para que solo acepte dígitos
     * y un máximo de 9 caracteres (formato de teléfono sin +51).
     */
    private void aplicarFiltroTelefono(JTextField campo) {
        campo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
                if (campo.getText().length() >= 9 && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
            }
        });
    }

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXTO_OSCURO);
        return lbl;
    }

    private JTextField crearCampo(String placeholder) {
        JTextField campo = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    g2.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        estilizarCampo(campo);
        return campo;
    }

    private void estilizarCampo(JComponent campo) {
        campo.setPreferredSize(new Dimension(0, 40));
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return lbl;
    }

    private JLabel etiquetaPasoGris(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(255, 255, 255, 120));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return lbl;
    }
}
