package view;

import controller.AutenticacionController;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * ============================================================
 * CAPA: VIEW — Vista de Registro de Nuevo Cliente
 * ============================================================
 * Permite que cualquier persona cree una cuenta CLIENTE.
 * El controlador se encarga de hashear la contraseña antes
 * de persistirla en la base de datos.
 *
 * Campos: primer nombre, apellido paterno, apellido materno
 *         (opcional), email, teléfono, contraseña,
 *         confirmación de contraseña.
 *
 * Clase: RegistroView
 * Módulo: Gestión de Usuarios y Autenticación — QoriBank
 * ============================================================
 */
public class RegistroView extends JFrame {

    private static final Color DORADO_PRINCIPAL = LoginView.DORADO_PRINCIPAL;
    private static final Color DORADO_OSCURO    = LoginView.DORADO_OSCURO;
    private static final Color DORADO_CLARO     = LoginView.DORADO_CLARO;
    private static final Color GRIS_FONDO       = LoginView.GRIS_FONDO;
    private static final Color BLANCO           = LoginView.BLANCO;
    private static final Color TEXTO_OSCURO     = LoginView.TEXTO_OSCURO;
    private static final Color GRIS_CAMPO       = LoginView.GRIS_CAMPO;
    private static final Color ROJO_ERROR       = LoginView.ROJO_ERROR;
    private static final Color VERDE_EXITO      = new Color(0x27AE60);

    private JTextField    txtNombre;
    private JTextField    txtApellidoP;
    private JTextField    txtApellidoM;
    private JTextField    txtEmail;
    private JTextField    txtTelefono;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirm;
    private JLabel        lblError;
    private JLabel        lblExito;

    private final AutenticacionController controller;

    public RegistroView() {
        this.controller = new AutenticacionController();
        initUI();
    }

    private void initUI() {
        setTitle("Qori Bank — Crear Cuenta");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 640);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GRIS_FONDO);
        setContentPane(root);

        root.add(crearBarraLateral(), BorderLayout.WEST);
        root.add(crearPanelRegistro(), BorderLayout.CENTER);
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

        barra.add(Box.createVerticalGlue());

        JLabel lblIcono = new JLabel("\u26BF") {
            { setFont(new Font("Segoe UI Symbol", Font.PLAIN, 60)); }
        };
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.add(lblIcono);

        barra.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel lblNombre = new JLabel("Qori Bank");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblNombre.setForeground(BLANCO);
        lblNombre.setHorizontalAlignment(SwingConstants.CENTER);
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblNombre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        barra.add(lblNombre);

        barra.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel lblSub = new JLabel("<html><center>Crea tu cuenta<br>y empieza hoy</center></html>");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(255, 255, 255, 200));
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSub.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        lblSub.setPreferredSize(new Dimension(200, 40));
        barra.add(lblSub);

        barra.add(Box.createVerticalGlue());

        // Pasos visuales
        for (String paso : new String[]{
                "Datos personales",
                "Credenciales de acceso",
                "Confirmar registro"}) {
            JLabel lbl = new JLabel(paso);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(BLANCO);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            barra.add(lbl);
            barra.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        barra.add(Box.createRigidArea(new Dimension(0, 12)));

        JLabel lblPie = new JLabel("© 2026 Qori Bank");
        lblPie.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPie.setForeground(new Color(255, 255, 255, 150));
        lblPie.setHorizontalAlignment(SwingConstants.CENTER);
        lblPie.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPie.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        barra.add(lblPie);

        return barra;
    }

    // --------------------------------------------------------
    // Panel de registro
    // --------------------------------------------------------
    private JPanel crearPanelRegistro() {
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
        card.setPreferredSize(new Dimension(460, 570));
        card.setBorder(BorderFactory.createEmptyBorder(28, 40, 28, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.gridx   = 0;
        gbc.weightx = 1.0;

        // Título
        JLabel lblTitulo = new JLabel("Crear Cuenta");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(TEXTO_OSCURO);
        gbc.gridy  = 0;
        gbc.insets = new Insets(0, 0, 2, 0);
        card.add(lblTitulo, gbc);

        JLabel lblSub = new JLabel("Ingresa tus datos para registrarte como cliente.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(Color.GRAY);
        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(lblSub, gbc);

        // Nombres en una fila (2 columnas)
        gbc.gridy  = 2;
        gbc.insets = new Insets(4, 0, 2, 0);
        card.add(crearLabel("Primer Nombre *"), gbc);
        txtNombre = crearCampo("Ej: Carlos");
        gbc.gridy  = 3;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(txtNombre, gbc);

        gbc.gridy  = 4;
        gbc.insets = new Insets(4, 0, 2, 0);
        card.add(crearLabel("Apellido Paterno *"), gbc);
        txtApellidoP = crearCampo("Ej: Quispe");
        gbc.gridy  = 5;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(txtApellidoP, gbc);

        gbc.gridy  = 6;
        gbc.insets = new Insets(4, 0, 2, 0);
        card.add(crearLabel("Apellido Materno (opcional)"), gbc);
        txtApellidoM = crearCampo("Ej: Mamani");
        gbc.gridy  = 7;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(txtApellidoM, gbc);

        gbc.gridy  = 8;
        gbc.insets = new Insets(4, 0, 2, 0);
        card.add(crearLabel("Correo Electrónico *"), gbc);
        txtEmail = crearCampo("correo@ejemplo.com");
        gbc.gridy  = 9;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(txtEmail, gbc);

        gbc.gridy  = 10;
        gbc.insets = new Insets(4, 0, 2, 0);
        card.add(crearLabel("Teléfono * (9 dígitos, sin +51)"), gbc);
        txtTelefono = crearCampo("Ej: 987654321");
        aplicarFiltroTelefono(txtTelefono);
        gbc.gridy  = 11;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(txtTelefono, gbc);

        gbc.gridy  = 12;
        gbc.insets = new Insets(4, 0, 2, 0);
        card.add(crearLabel("Contraseña * (mín. 6 caracteres)"), gbc);
        txtPassword = new JPasswordField();
        estilizarCampo(txtPassword);
        gbc.gridy  = 13;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(txtPassword, gbc);

        gbc.gridy  = 14;
        gbc.insets = new Insets(4, 0, 2, 0);
        card.add(crearLabel("Confirmar Contraseña *"), gbc);
        txtConfirm = new JPasswordField();
        estilizarCampo(txtConfirm);
        gbc.gridy  = 15;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(txtConfirm, gbc);

        // Mensajes de feedback
        lblError = new JLabel("");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(ROJO_ERROR);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy  = 16;
        gbc.insets = new Insets(0, 0, 2, 0);
        card.add(lblError, gbc);

        lblExito = new JLabel("");
        lblExito.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblExito.setForeground(VERDE_EXITO);
        lblExito.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy  = 17;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblExito, gbc);

        // Botón Registrarse
        JButton btnRegistrar = crearBotonPrincipal("Crear Cuenta");
        btnRegistrar.addActionListener(e -> procesarRegistro());
        gbc.gridy  = 18;
        gbc.insets = new Insets(4, 0, 8, 0);
        card.add(btnRegistrar, gbc);

        // Separador
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0xDDDDDD));
        gbc.gridy  = 19;
        gbc.insets = new Insets(4, 0, 8, 0);
        card.add(sep, gbc);

        // Volver al login
        JButton btnVolver = new JButton("¿Ya tienes cuenta? Inicia sesión");
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
        gbc.gridy  = 20;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(btnVolver, gbc);

        panelOuter.add(card);
        return panelOuter;
    }

    // --------------------------------------------------------
    // Evento: Registrar
    // --------------------------------------------------------
    private void procesarRegistro() {
        lblError.setText("");
        lblExito.setText("");

        String password = new String(txtPassword.getPassword());
        String confirm  = new String(txtConfirm.getPassword());

        // Validación local: contraseñas coinciden
        if (!password.equals(confirm)) {
            lblError.setText("Las contraseñas no coinciden.");
            txtConfirm.setText("");
            return;
        }

        // Delegar al controlador (hashea internamente)
        String error = controller.registrarCliente(
                txtNombre.getText(),
                txtApellidoP.getText(),
                txtApellidoM.getText(),
                txtEmail.getText(),
                txtTelefono.getText(),
                password
        );

        if (error != null) {
            lblError.setText(error);
            return;
        }

        // Éxito
        lblExito.setText("✅ ¡Cuenta creada! Redirigiendo al inicio de sesión...");
        limpiarCampos();

        // Esperar 2 segundos y volver al login
        Timer timer = new Timer(2000, e -> {
            new LoginView().setVisible(true);
            this.dispose();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtApellidoP.setText("");
        txtApellidoM.setText("");
        txtEmail.setText("");
        txtTelefono.setText("");
        txtPassword.setText("");
        txtConfirm.setText("");
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
        campo.setPreferredSize(new Dimension(0, 38));
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(0, 44));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
