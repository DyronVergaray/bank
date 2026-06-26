package view;

import controller.AutenticacionController;
import controller.TransferenciaController;
import model.InicioSesion;
import model.Transferencia;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * ============================================================
 * CAPA: VIEW — Vista de Verificación OTP para Transferencia
 * ============================================================
 * Misma idea visual que VerificacionCodigoView pero conectada
 * a TransferenciaController. Al confirmar el código, delega
 * el procesamiento completo a TransferenciaFacade (FACADE).
 *
 * Clase: VerificacionTransferenciaView
 * Módulo: Transferencias — QoriBank
 * ============================================================
 */
public class VerificacionTransferenciaView extends JFrame {

    private static final Color DORADO_PRINCIPAL = LoginView.DORADO_PRINCIPAL;
    private static final Color GRIS_FONDO       = LoginView.GRIS_FONDO;
    private static final Color BLANCO           = LoginView.BLANCO;
    private static final Color TEXTO_OSCURO     = LoginView.TEXTO_OSCURO;
    private static final Color GRIS_CAMPO       = LoginView.GRIS_CAMPO;
    private static final Color ROJO_ERROR       = LoginView.ROJO_ERROR;
    private static final Color VERDE_TRANS      = new Color(0x00897B);

    private JTextField txtCodigo;
    private JLabel     lblError;

    private final AutenticacionController  authController;
    private final TransferenciaController  transController;
    private final InicioSesion.CanalVerificacion canal;
    private final TransferenciasView       origen;
    private final String                   codigoSimulado;

    public VerificacionTransferenciaView(AutenticacionController authController,
                                         TransferenciaController transController,
                                         InicioSesion.CanalVerificacion canal,
                                         String codigoSimulado,
                                         TransferenciasView origen) {
        this.authController  = authController;
        this.transController = transController;
        this.canal           = canal;
        this.codigoSimulado  = codigoSimulado;
        this.origen          = origen;
        initUI();
    }

    private void initUI() {
        setTitle("Qori Bank — Confirmar Transferencia");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GRIS_FONDO);
        setContentPane(root);

        root.add(crearBarraLateral(), BorderLayout.WEST);
        root.add(crearPanelVerificacion(), BorderLayout.CENTER);
    }

    // --------------------------------------------------------
    // Barra lateral
    // --------------------------------------------------------
    private JPanel crearBarraLateral() {
        JPanel barra = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0,0,VERDE_TRANS,0,getHeight(),new Color(0x00695C)));
                g2.fillRect(0,0,getWidth(),getHeight());
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

        JLabel lblTitulo = new JLabel("Confirmar");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(BLANCO);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        barra.add(lblTitulo);

        barra.add(Box.createRigidArea(new Dimension(0, 10)));

        // Resumen de la transferencia
        Transferencia t = transController.getTransferenciaActual();
        if (t != null) {
            JLabel lblResumen = new JLabel(
                    "<html><center>S/ " + String.format("%,.2f", t.getMonto())
                    + "<br>→ " + t.getNumeroTarjetaDestinoEnmascarado()
                    + "<br>" + t.getEntidadDestino() + "</center></html>");
            lblResumen.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblResumen.setForeground(new Color(255,255,255,220));
            lblResumen.setHorizontalAlignment(SwingConstants.CENTER);
            lblResumen.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblResumen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            lblResumen.setPreferredSize(new Dimension(200, 60));
            barra.add(lblResumen);
        }

        barra.add(Box.createVerticalGlue());

        for (String paso : new String[]{"Datos ingresados", "Confirmar código", "Transferencia enviada"}) {
            JLabel lbl = new JLabel(paso);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(BLANCO);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            barra.add(lbl);
            barra.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        barra.add(Box.createRigidArea(new Dimension(0, 10)));
        return barra;
    }

    // --------------------------------------------------------
    // Panel de verificación
    // --------------------------------------------------------
    private JPanel crearPanelVerificacion() {
        JPanel panelOuter = new JPanel(new GridBagLayout());
        panelOuter.setBackground(GRIS_FONDO);

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLANCO);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),20,20));
                g2.dispose();
            }
        };
        card.setLayout(new GridBagLayout());
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(420, 400));
        card.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.weightx = 1.0;

        JLabel lblTitulo = new JLabel("Confirmar Transferencia");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(TEXTO_OSCURO);
        gbc.gridy = 0; gbc.insets = new Insets(0,0,4,0);
        card.add(lblTitulo, gbc);

        String canalDesc = canal == InicioSesion.CanalVerificacion.SMS
                ? "al número de teléfono registrado" : "al correo electrónico registrado";
        JLabel lblSub = new JLabel(
                "<html>Ingrese el código de 6 dígitos enviado<br>" + canalDesc + ".</html>");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(Color.GRAY);
        gbc.gridy = 1; gbc.insets = new Insets(0,0,20,0);
        card.add(lblSub, gbc);

        // Panel simulación
        JPanel panelSim = new JPanel(new BorderLayout());
        panelSim.setBackground(new Color(0xE8F5E9));
        panelSim.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VERDE_TRANS, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        JLabel lblSim = new JLabel(
                "<html><b>Simulación:</b> Código enviado &rarr; <span style='font-size:16pt'>"
                + codigoSimulado + "</span></html>");
        lblSim.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSim.setForeground(new Color(0x1B5E20));
        panelSim.add(lblSim, BorderLayout.CENTER);
        gbc.gridy = 2; gbc.insets = new Insets(0,0,20,0);
        card.add(panelSim, gbc);

        JLabel lblCodigo = new JLabel("Código de confirmación");
        lblCodigo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCodigo.setForeground(TEXTO_OSCURO);
        gbc.gridy = 3; gbc.insets = new Insets(0,0,4,0);
        card.add(lblCodigo, gbc);

        txtCodigo = new JTextField();
        txtCodigo.setPreferredSize(new Dimension(0, 52));
        txtCodigo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        txtCodigo.setHorizontalAlignment(JTextField.CENTER);
        txtCodigo.setBackground(GRIS_CAMPO);
        txtCodigo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCCCCCC), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        txtCodigo.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE) e.consume();
                if (txtCodigo.getText().length() >= 6 && c != KeyEvent.VK_BACK_SPACE) e.consume();
            }
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) procesarConfirmacion();
            }
        });
        gbc.gridy = 4; gbc.insets = new Insets(0,0,8,0);
        card.add(txtCodigo, gbc);

        JLabel lblExpira = new JLabel("Válido por 10 minutos");
        lblExpira.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblExpira.setForeground(Color.GRAY);
        gbc.gridy = 5; gbc.insets = new Insets(0,0,8,0);
        card.add(lblExpira, gbc);

        lblError = new JLabel("");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(ROJO_ERROR);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 6; gbc.insets = new Insets(0,0,4,0);
        card.add(lblError, gbc);

        JButton btnConfirmar = crearBotonPrincipal("Confirmar Transferencia");
        btnConfirmar.addActionListener(e -> procesarConfirmacion());
        gbc.gridy = 7; gbc.insets = new Insets(4,0,10,0);
        card.add(btnConfirmar, gbc);

        JButton btnCancelar = new JButton("← Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancelar.setForeground(VERDE_TRANS);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setContentAreaFilled(false);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> {
            transController.limpiarEstado();
            origen.refrescarAlVolver();
            this.dispose();
        });
        gbc.gridy = 8; gbc.insets = new Insets(0,0,0,0);
        card.add(btnCancelar, gbc);

        panelOuter.add(card);
        return panelOuter;
    }

    // --------------------------------------------------------
    // Evento: confirmar transferencia
    // --------------------------------------------------------
    private void procesarConfirmacion() {
        String codigo = txtCodigo.getText().trim();
        if (codigo.length() != 6) {
            lblError.setText("El código debe tener exactamente 6 dígitos.");
            return;
        }

        String error = transController.procesarTransferencia(codigo, canal);
        if (error != null) {
            lblError.setText(error);
            txtCodigo.setText("");
            txtCodigo.requestFocus();
            return;
        }

        // Transferencia exitosa
        Transferencia t = transController.getTransferenciaActual();
        JOptionPane.showMessageDialog(this,
                "<html><b>Transferencia exitosa</b><br>" +
                "Monto: S/ " + String.format("%,.2f", t.getMonto()) + "<br>" +
                "Destino: " + t.getNumeroTarjetaDestinoEnmascarado() +
                " (" + t.getEntidadDestino() + ")</html>",
                "Transferencia completada", JOptionPane.INFORMATION_MESSAGE);

        transController.limpiarEstado();
        origen.refrescarAlVolver();
        this.dispose();
    }

    private JButton crearBotonPrincipal(String texto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? VERDE_TRANS.darker()
                        : getModel().isRollover() ? VERDE_TRANS.brighter() : VERDE_TRANS);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                g2.setColor(BLANCO);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(0, 46));
        btn.setBorderPainted(false); btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
