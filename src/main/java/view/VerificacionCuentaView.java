package view;

import controller.AutenticacionController;
import controller.CuentaController;
import model.InicioSesion;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * ============================================================
 * CAPA: VIEW — Vista de Verificación de Código (Vinculación de Cuenta)
 * ============================================================
 * Misma idea visual que VerificacionCodigoView (Módulo 1), pero
 * conectada a CuentaController: confirma que la tarjeta recién
 * registrada pertenece al usuario.
 *
 * Clase: VerificacionCuentaView
 * Módulo: Vinculación de Cuentas Bancarias — QoriBank
 * ============================================================
 */
public class VerificacionCuentaView extends JFrame {

    private static final Color DORADO_PRINCIPAL = LoginView.DORADO_PRINCIPAL;
    private static final Color DORADO_OSCURO    = LoginView.DORADO_OSCURO;
    private static final Color DORADO_CLARO     = LoginView.DORADO_CLARO;
    private static final Color GRIS_FONDO       = LoginView.GRIS_FONDO;
    private static final Color BLANCO           = LoginView.BLANCO;
    private static final Color TEXTO_OSCURO     = LoginView.TEXTO_OSCURO;
    private static final Color GRIS_CAMPO       = LoginView.GRIS_CAMPO;
    private static final Color ROJO_ERROR       = LoginView.ROJO_ERROR;
    private static final Color MORADO_CUENTAS   = new Color(0x6C5CE7);

    private JTextField txtCodigo;
    private JLabel     lblError;
    private JLabel     lblSimulacion;

    private final AutenticacionController authController;
    private final CuentaController        cuentaController;
    private final InicioSesion.CanalVerificacion canal;
    private final MisCuentasView          origen;

    public VerificacionCuentaView(AutenticacionController authController,
                                  CuentaController cuentaController,
                                  InicioSesion.CanalVerificacion canal,
                                  String codigoSimulado,
                                  MisCuentasView origen) {
        this.authController   = authController;
        this.cuentaController = cuentaController;
        this.canal             = canal;
        this.origen            = origen;
        initUI(codigoSimulado);
    }

    private void initUI(String codigoSimulado) {
        setTitle("Qori Bank — Verificación de Cuenta Bancaria");
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

        JLabel lblIcono = new JLabel("\u26BF") {
            { setFont(new Font("Segoe UI Symbol", Font.PLAIN, 60)); }
        };
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.add(lblIcono);

        barra.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel lblTitulo = new JLabel("Verificación");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(BLANCO);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        barra.add(lblTitulo);

        barra.add(Box.createRigidArea(new Dimension(0, 10)));

        String canalNombre = canal == InicioSesion.CanalVerificacion.SMS ? "SMS" : "Correo";
        JLabel lblDesc = new JLabel(
                "<html><center>Código enviado por<br><b>" + canalNombre + "</b></center></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(new Color(255, 255, 255, 200));
        lblDesc.setHorizontalAlignment(SwingConstants.CENTER);
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        lblDesc.setPreferredSize(new Dimension(200, 40));
        barra.add(lblDesc);

        barra.add(Box.createVerticalGlue());

        JLabel lblPaso1 = etiquetaPaso("Datos de la tarjeta");
        JLabel lblPaso2 = etiquetaPaso("Ingresar código");
        JLabel lblPaso3 = etiquetaPasoGris("Cuenta vinculada");
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

        JLabel lblTitulo = new JLabel("Confirmar tu tarjeta");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(TEXTO_OSCURO);
        gbc.gridy  = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblTitulo, gbc);

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

        JPanel panelSim = new JPanel(new BorderLayout());
        panelSim.setBackground(new Color(0xFFF8E1));
        panelSim.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DORADO_PRINCIPAL, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        lblSimulacion = new JLabel(
                "<html><b>Simulación:</b> Código enviado &rarr; <span style='font-size:16pt'>"
                + codigoSimulado + "</span></html>");
        lblSimulacion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSimulacion.setForeground(new Color(0x7A5000));
        panelSim.add(lblSimulacion, BorderLayout.CENTER);

        gbc.gridy  = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(panelSim, gbc);

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

        JLabel lblExpira = new JLabel("Válido por 10 minutos");
        lblExpira.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblExpira.setForeground(Color.GRAY);
        gbc.gridy  = 5;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(lblExpira, gbc);

        lblError = new JLabel("");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(ROJO_ERROR);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy  = 6;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblError, gbc);

        JButton btnVerificar = crearBotonPrincipal("Verificar Código");
        btnVerificar.addActionListener(e -> procesarVerificacion());
        gbc.gridy  = 7;
        gbc.insets = new Insets(4, 0, 10, 0);
        card.add(btnVerificar, gbc);

        JButton btnCancelar = new JButton("← Cancelar vinculación");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancelar.setForeground(MORADO_CUENTAS);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setContentAreaFilled(false);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> cancelar());
        gbc.gridy  = 8;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(btnCancelar, gbc);

        panelOuter.add(card);
        return panelOuter;
    }

    // --------------------------------------------------------
    // Evento: verificar código
    // --------------------------------------------------------
    private void procesarVerificacion() {
        String codigo = txtCodigo.getText().trim();

        if (codigo.length() != 6) {
            lblError.setText("El código debe tener exactamente 6 dígitos.");
            return;
        }

        String error = cuentaController.verificarCodigoVinculacion(codigo, canal);

        if (error != null) {
            lblError.setText(error);
            txtCodigo.setText("");
            txtCodigo.requestFocus();
            return;
        }

        JOptionPane.showMessageDialog(this,
                "¡Tu cuenta bancaria fue vinculada exitosamente!",
                "Cuenta vinculada — QoriBank", JOptionPane.INFORMATION_MESSAGE);

        origen.refrescarAlVolver();
        this.dispose();
    }

    // --------------------------------------------------------
    // Evento: cancelar la vinculación en curso
    // --------------------------------------------------------
    private void cancelar() {
        cuentaController.cancelarRegistroPendiente();
        origen.refrescarAlVolver();
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
