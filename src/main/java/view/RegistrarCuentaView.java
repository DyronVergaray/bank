package view;

import controller.AutenticacionController;
import controller.CuentaController;
import model.InicioSesion;
import model.TipoCuentaBancaria;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * ============================================================
 * CAPA: VIEW — Vista de Registro de Cuenta Bancaria
 * ============================================================
 * Formulario donde el cliente ingresa los datos de su tarjeta
 * (banco, número, vencimiento, CVV) y elige el canal OTP para
 * confirmar que la tarjeta le pertenece.
 *
 * Clase: RegistrarCuentaView
 * Módulo: Vinculación de Cuentas Bancarias — QoriBank
 * ============================================================
 */
public class RegistrarCuentaView extends JFrame {

    private static final Color DORADO_PRINCIPAL = LoginView.DORADO_PRINCIPAL;
    private static final Color DORADO_OSCURO    = LoginView.DORADO_OSCURO;
    private static final Color DORADO_CLARO     = LoginView.DORADO_CLARO;
    private static final Color GRIS_FONDO       = LoginView.GRIS_FONDO;
    private static final Color BLANCO           = LoginView.BLANCO;
    private static final Color TEXTO_OSCURO     = LoginView.TEXTO_OSCURO;
    private static final Color GRIS_CAMPO       = LoginView.GRIS_CAMPO;
    private static final Color ROJO_ERROR       = LoginView.ROJO_ERROR;
    private static final Color MORADO_CUENTAS   = new Color(0x6C5CE7);

    private final AutenticacionController authController;
    private final CuentaController        cuentaController;
    private final MisCuentasView          origen;

    private JComboBox<TipoCuentaBancaria> cmbBanco;
    private JTextField   txtNumeroTarjeta;
    private JTextField   txtVencimiento;
    private JPasswordField txtCvv;
    private JTextField   txtSaldoActual;
    private JRadioButton rbSMS;
    private JRadioButton rbCorreo;
    private JLabel        lblError;

    public RegistrarCuentaView(AutenticacionController authController, MisCuentasView origen) {
        this.authController  = authController;
        this.origen           = origen;
        this.cuentaController = new CuentaController(authController.getUsuarioActual());
        initUI();
    }

    private void initUI() {
        setTitle("Qori Bank — Vincular Cuenta Bancaria");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GRIS_FONDO);
        setContentPane(root);

        root.add(crearBarraLateral(), BorderLayout.WEST);
        root.add(crearPanelFormulario(), BorderLayout.CENTER);
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
                        0, 0, MORADO_CUENTAS, 0, getHeight(), new Color(0x4834A0));
                g2.setPaint(grad);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        barra.setPreferredSize(new Dimension(260, 0));
        barra.setLayout(new BoxLayout(barra, BoxLayout.Y_AXIS));
        barra.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        barra.add(Box.createVerticalGlue());

        JLabel lblIcono = new JLabel("\u25A4") {
            { setFont(new Font("Segoe UI Symbol", Font.PLAIN, 56)); }
        };
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.add(lblIcono);

        barra.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel lblTitulo = new JLabel("Vincular Cuenta");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(BLANCO);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        barra.add(lblTitulo);

        barra.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel lblDesc = new JLabel(
                "<html><center>Registra tu tarjeta de forma<br>segura y verificada</center></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(new Color(255, 255, 255, 200));
        lblDesc.setHorizontalAlignment(SwingConstants.CENTER);
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        lblDesc.setPreferredSize(new Dimension(200, 40));
        barra.add(lblDesc);

        barra.add(Box.createVerticalGlue());

        for (String paso : new String[]{"Datos de la tarjeta", "Verificación OTP", "Cuenta vinculada"}) {
            JLabel lbl = new JLabel(paso);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(BLANCO);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            barra.add(lbl);
            barra.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        return barra;
    }

    // --------------------------------------------------------
    // Panel de formulario
    // --------------------------------------------------------
    private JPanel crearPanelFormulario() {
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
        card.setPreferredSize(new Dimension(440, 650));
        card.setBorder(BorderFactory.createEmptyBorder(28, 40, 28, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.gridx   = 0;
        gbc.weightx = 1.0;

        JLabel lblTitulo = new JLabel("Datos de la tarjeta");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(TEXTO_OSCURO);
        gbc.gridy  = 0;
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(lblTitulo, gbc);

        // Banco
        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 0, 2, 0);
        card.add(crearLabel("Entidad bancaria"), gbc);
        cmbBanco = new JComboBox<>();
        cargarBancos();
        estilizarCombo(cmbBanco);
        gbc.gridy  = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        card.add(cmbBanco, gbc);

        // Número de tarjeta
        gbc.gridy  = 3;
        gbc.insets = new Insets(0, 0, 2, 0);
        card.add(crearLabel("Número de tarjeta (16 dígitos)"), gbc);
        txtNumeroTarjeta = crearCampo("1234567812345678");
        aplicarFiltroNumerico(txtNumeroTarjeta, 16);
        gbc.gridy  = 4;
        gbc.insets = new Insets(0, 0, 10, 0);
        card.add(txtNumeroTarjeta, gbc);

        // Vencimiento + CVV en una fila
        JPanel filaVencCvv = new JPanel(new GridLayout(1, 2, 12, 0));
        filaVencCvv.setOpaque(false);

        JPanel colVenc = new JPanel();
        colVenc.setLayout(new BoxLayout(colVenc, BoxLayout.Y_AXIS));
        colVenc.setOpaque(false);
        JLabel lblVenc = crearLabel("Vencimiento (MM/AAAA)");
        lblVenc.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtVencimiento = crearCampo("MM/AAAA");
        txtVencimiento.setAlignmentX(Component.LEFT_ALIGNMENT);
        colVenc.add(lblVenc);
        colVenc.add(Box.createRigidArea(new Dimension(0, 2)));
        colVenc.add(txtVencimiento);

        JPanel colCvv = new JPanel();
        colCvv.setLayout(new BoxLayout(colCvv, BoxLayout.Y_AXIS));
        colCvv.setOpaque(false);
        JLabel lblCvv = crearLabel("CVV");
        lblCvv.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtCvv = new JPasswordField();
        estilizarCampo(txtCvv);
        aplicarFiltroNumerico(txtCvv, 4);
        txtCvv.setAlignmentX(Component.LEFT_ALIGNMENT);
        colCvv.add(lblCvv);
        colCvv.add(Box.createRigidArea(new Dimension(0, 2)));
        colCvv.add(txtCvv);

        filaVencCvv.add(colVenc);
        filaVencCvv.add(colCvv);

        gbc.gridy  = 5;
        gbc.insets = new Insets(0, 0, 10, 0);
        card.add(filaVencCvv, gbc);

        // Saldo actual de la tarjeta
        gbc.gridy  = 6;
        gbc.insets = new Insets(0, 0, 2, 0);
        card.add(crearLabel("Saldo actual (S/)"), gbc);
        txtSaldoActual = crearCampo("0.00");
        aplicarFiltroDecimal(txtSaldoActual);
        gbc.gridy  = 7;
        gbc.insets = new Insets(0, 0, 12, 0);
        card.add(txtSaldoActual, gbc);

        // Canal de verificación
        gbc.gridy  = 8;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(crearLabel("Verificar propiedad de la tarjeta por:"), gbc);

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
        gbc.gridy  = 9;
        gbc.insets = new Insets(2, 0, 12, 0);
        card.add(panelCanal, gbc);

        // Error
        lblError = new JLabel("");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(ROJO_ERROR);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy  = 10;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblError, gbc);

        // Botón continuar
        JButton btnContinuar = crearBotonPrincipal("Continuar");
        btnContinuar.addActionListener(e -> procesarRegistro());
        gbc.gridy  = 11;
        gbc.insets = new Insets(4, 0, 8, 0);
        card.add(btnContinuar, gbc);

        // Volver
        JButton btnVolver = new JButton("← Cancelar");
        btnVolver.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnVolver.setForeground(MORADO_CUENTAS);
        btnVolver.setBorderPainted(false);
        btnVolver.setContentAreaFilled(false);
        btnVolver.setFocusPainted(false);
        btnVolver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> volverAMisCuentas());
        gbc.gridy  = 12;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(btnVolver, gbc);

        panelOuter.add(card);
        return panelOuter;
    }

    // --------------------------------------------------------
    // Cargar bancos disponibles en el combo
    // --------------------------------------------------------
    private void cargarBancos() {
        List<TipoCuentaBancaria> tipos = cuentaController.obtenerTiposCuentaDisponibles();
        for (TipoCuentaBancaria t : tipos) {
            cmbBanco.addItem(t);
        }
        cmbBanco.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TipoCuentaBancaria) {
                    setText(((TipoCuentaBancaria) value).getNombreEntidad());
                }
                return this;
            }
        });
    }

    // --------------------------------------------------------
    // Evento: Continuar (registrar + enviar OTP)
    // --------------------------------------------------------
    private void procesarRegistro() {
        lblError.setText("");

        TipoCuentaBancaria bancoSeleccionado = (TipoCuentaBancaria) cmbBanco.getSelectedItem();
        if (bancoSeleccionado == null) {
            lblError.setText("Seleccione una entidad bancaria.");
            return;
        }

        String numeroTarjeta = txtNumeroTarjeta.getText().trim();
        String vencimiento    = txtVencimiento.getText().trim();
        String cvv            = new String(txtCvv.getPassword());
        String saldoTexto     = txtSaldoActual.getText().trim();

        java.math.BigDecimal saldoActual;
        try {
            saldoActual = new java.math.BigDecimal(saldoTexto.isEmpty() ? "0" : saldoTexto);
        } catch (NumberFormatException ex) {
            lblError.setText("Ingrese un saldo válido (ej: 1500.50).");
            return;
        }

        String errorRegistro = cuentaController.registrarCuenta(
                bancoSeleccionado.getIdTipoCuenta(), numeroTarjeta, vencimiento, cvv, saldoActual);

        if (errorRegistro != null) {
            lblError.setText(errorRegistro);
            return;
        }

        InicioSesion.CanalVerificacion canal = rbSMS.isSelected()
                ? InicioSesion.CanalVerificacion.SMS
                : InicioSesion.CanalVerificacion.CORREO;

        String errorOtp = cuentaController.enviarCodigoVinculacion(canal);
        if (errorOtp != null) {
            lblError.setText(errorOtp);
            return;
        }

        // Abrir verificación de código (reutiliza VerificacionCodigoView)
        new VerificacionCuentaView(authController, cuentaController, canal,
                cuentaController.getCodigoGeneradoSimulacion(), origen).setVisible(true);
        this.dispose();
    }

    // --------------------------------------------------------
    // Evento: Cancelar
    // --------------------------------------------------------
    private void volverAMisCuentas() {
        origen.refrescarAlVolver();
        this.dispose();
    }

    // --------------------------------------------------------
    // Helpers de UI
    // --------------------------------------------------------
    private void aplicarFiltroNumerico(JTextField campo, int maxLen) {
        campo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
                if (campo.getText().length() >= maxLen && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
            }
        });
    }

    /**
     * Restringe un campo de texto para que solo acepte números
     * decimales positivos (dígitos y un único punto decimal),
     * usado para el campo de saldo actual de la tarjeta.
     */
    private void aplicarFiltroDecimal(JTextField campo) {
        campo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                boolean esDigito = Character.isDigit(c);
                boolean esPunto  = c == '.' && !campo.getText().contains(".");
                boolean esBorrar = c == KeyEvent.VK_BACK_SPACE;

                if (!esDigito && !esPunto && !esBorrar) {
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

    private void estilizarCombo(JComboBox<?> combo) {
        combo.setPreferredSize(new Dimension(0, 38));
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(GRIS_CAMPO);
        combo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
                g2.setColor(getModel().isPressed() ? MORADO_CUENTAS.darker()
                        : getModel().isRollover() ? MORADO_CUENTAS.brighter()
                        : MORADO_CUENTAS);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(BLANCO);
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
}
