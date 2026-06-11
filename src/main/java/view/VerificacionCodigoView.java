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
 * CAPA: VIEW — Vista de Verificación de Código OTP
 * ============================================================
 * Pantalla intermedia del flujo de autenticación:
 *   - Muestra a qué destino se envió el código
 *   - Campo para ingresar el código de 6 dígitos
 *   - Botón "Verificar" → delega al Controlador
 *   - Muestra el código en simulación (consola/panel info)
 *
 * Clase: VerificacionCodigoView
 * Módulo: Gestión de Usuarios y Autenticación — QoriBank
 * ============================================================
 */
public class VerificacionCodigoView extends JFrame {

    // Hereda la paleta de LoginView
    private static final Color DORADO_PRINCIPAL = LoginView.DORADO_PRINCIPAL;
    private static final Color DORADO_OSCURO    = LoginView.DORADO_OSCURO;
    private static final Color DORADO_CLARO     = LoginView.DORADO_CLARO;
    private static final Color GRIS_FONDO       = LoginView.GRIS_FONDO;
    private static final Color BLANCO           = LoginView.BLANCO;
    private static final Color TEXTO_OSCURO     = LoginView.TEXTO_OSCURO;
    private static final Color GRIS_CAMPO       = LoginView.GRIS_CAMPO;
    private static final Color ROJO_ERROR       = LoginView.ROJO_ERROR;

    // Componentes
    private JTextField txtCodigo;
    private JLabel     lblError;
    private JLabel     lblSimulacion; // Muestra el código en pantalla (simulación)

    // Dependencias MVC
    private final AutenticacionController controller;
    private final InicioSesion.CanalVerificacion canal;

    // --------------------------------------------------------
    // Constructor
    // --------------------------------------------------------
    public VerificacionCodigoView(AutenticacionController controller,
                                   InicioSesion.CanalVerificacion canal,
                                   String codigoSimulado) {
        this.controller = controller;
        this.canal      = canal;
        initUI(codigoSimulado);
    }

    // --------------------------------------------------------
    // Construcción de la interfaz
    // --------------------------------------------------------
    private void initUI(String codigoSimulado) {
        setTitle("Qori Bank — Verificación de Código");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GRIS_FONDO);
        setContentPane(root);

        root.add(crearBarraLateral(), BorderLayout.WEST);
        root.add(crearPanelVerificacion(codigoSimulado), BorderLayout.CENTER);
    }

    // --------------------------------------------------------
    // Barra lateral (reutiliza el estilo de LoginView)
    // --------------------------------------------------------
    private JPanel crearBarraLateral() {
        JPanel barra = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint grad = new GradientPaint(
                        0, 0, DORADO_PRINCIPAL,
                        0, getHeight(), DORADO_OSCURO);
                g2.setPaint(grad);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        barra.setPreferredSize(new Dimension(260, 0));
        barra.setLayout(new BoxLayout(barra, BoxLayout.Y_AXIS));
        barra.setBorder(BorderFactory.createEmptyBorder(50, 30, 30, 30));

        // Ícono del candado de seguridad
        JLabel lblIcono = new JLabel("🔐") {
            { setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60)); }
        };
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.add(lblIcono);

        barra.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel lblTitulo = new JLabel("Verificación");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(BLANCO);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.add(lblTitulo);

        barra.add(Box.createRigidArea(new Dimension(0, 10)));

        String canalNombre = canal == InicioSesion.CanalVerificacion.SMS
                ? "SMS" : "Correo";
        JLabel lblDesc = new JLabel(
                "<html><center>Código enviado por<br><b>" + canalNombre + "</b></center></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(new Color(255, 255, 255, 200));
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.add(lblDesc);

        barra.add(Box.createVerticalGlue());

        // Pasos visuales del flujo
        JLabel lblPaso1 = etiquetaPaso("✅  Credenciales validadas");
        JLabel lblPaso2 = etiquetaPaso("▶  Ingresar código");
        JLabel lblPaso3 = etiquetaPasoGris("○  Acceso al sistema");
        barra.add(lblPaso1);
        barra.add(Box.createRigidArea(new Dimension(0, 8)));
        barra.add(lblPaso2);
        barra.add(Box.createRigidArea(new Dimension(0, 8)));
        barra.add(lblPaso3);
        barra.add(Box.createRigidArea(new Dimension(0, 20)));

        return barra;
    }

    // --------------------------------------------------------
    // Panel principal de verificación
    // --------------------------------------------------------
    private JPanel crearPanelVerificacion(String codigoSimulado) {
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
        card.setPreferredSize(new Dimension(420, 430));
        card.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.gridx   = 0;
        gbc.weightx = 1.0;

        // Título
        JLabel lblTitulo = new JLabel("Verificación en dos pasos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(TEXTO_OSCURO);
        gbc.gridy  = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblTitulo, gbc);

        // Subtítulo con canal
        String canalDesc = canal == InicioSesion.CanalVerificacion.SMS
                ? "al número de teléfono registrado"
                : "al correo electrónico registrado";
        JLabel lblSub = new JLabel(
                "<html>Ingrese el código de 6 dígitos enviado<br>" + canalDesc + ".</html>");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(Color.GRAY);
        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(lblSub, gbc);

        // Panel de simulación (muestra el código generado — solo para pruebas)
        JPanel panelSim = new JPanel(new BorderLayout());
        panelSim.setBackground(new Color(0xFFF8E1));
        panelSim.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DORADO_PRINCIPAL, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        lblSimulacion = new JLabel(
                "<html><b>🧪 Simulación:</b> Código enviado → <span style='font-size:16pt'>"
                + codigoSimulado + "</span></html>");
        lblSimulacion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSimulacion.setForeground(new Color(0x7A5000));
        panelSim.add(lblSimulacion, BorderLayout.CENTER);

        gbc.gridy  = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(panelSim, gbc);

        // Campo para el código
        JLabel lblCodigo = new JLabel("Código de verificación");
        lblCodigo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCodigo.setForeground(TEXTO_OSCURO);
        gbc.gridy  = 3;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblCodigo, gbc);

        txtCodigo = new JTextField();
        txtCodigo.setPreferredSize(new Dimension(0, 52));
        txtCodigo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        txtCodigo.setHorizontalAlignment(JTextField.CENTER);
        txtCodigo.setBackground(GRIS_CAMPO);
        txtCodigo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCCCCCC), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        // Solo aceptar dígitos
        txtCodigo.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
                if (txtCodigo.getText().length() >= 6 && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
            }
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) procesarVerificacion();
            }
        });
        gbc.gridy  = 4;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(txtCodigo, gbc);

        JLabel lblExpira = new JLabel("⏱ Válido por 10 minutos");
        lblExpira.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblExpira.setForeground(Color.GRAY);
        gbc.gridy  = 5;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(lblExpira, gbc);

        // Etiqueta de error
        lblError = new JLabel("");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(ROJO_ERROR);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy  = 6;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblError, gbc);

        // Botón Verificar
        JButton btnVerificar = crearBotonPrincipal("Verificar Código");
        btnVerificar.addActionListener(e -> procesarVerificacion());
        gbc.gridy  = 7;
        gbc.insets = new Insets(4, 0, 10, 0);
        card.add(btnVerificar, gbc);

        // Link volver
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
        gbc.gridy  = 8;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(btnVolver, gbc);

        panelOuter.add(card);
        return panelOuter;
    }

    // --------------------------------------------------------
    // Evento: verificar código
    // --------------------------------------------------------
    private void procesarVerificacion() {
        String codigo = txtCodigo.getText().trim();

        // Validación local: 6 dígitos
        if (codigo.length() != 6) {
            lblError.setText("El código debe tener exactamente 6 dígitos.");
            return;
        }

        // Delegar al controlador (MVC)
        String error = controller.verificarCodigo(codigo);

        if (error != null) {
            lblError.setText(error);
            txtCodigo.setText("");
            txtCodigo.requestFocus();
            return;
        }

        // Autenticación exitosa → abrir pantalla de bienvenida
        BienvenidaView ventanaBienvenida = new BienvenidaView(controller);
        ventanaBienvenida.setVisible(true);
        this.dispose();
    }

    // --------------------------------------------------------
    // Helpers
    // --------------------------------------------------------
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

    private JLabel etiquetaPasoGris(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(255, 255, 255, 120));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }
}
