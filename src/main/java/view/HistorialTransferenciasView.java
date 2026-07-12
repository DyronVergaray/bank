package view;

import controller.AutenticacionController;
import controller.NotificacionController;
import controller.TransferenciaController;
import model.InicioSesion;
import model.Notificacion;
import model.Transferencia;
import model.Usuario;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ============================================================
 * CAPA: VIEW — Vista de Historial de Transferencias
 * ============================================================
 * Muestra todas las transferencias del usuario en una tabla con:
 *   - Fecha, tipo, tarjeta origen, tarjeta destino, entidad
 *     destino, monto, estado
 * Permite descargar el comprobante de cada transferencia
 * exitosa como archivo .txt.
 *
 * Clase: HistorialTransferenciasView
 * Módulo: Transferencias — QoriBank
 * ============================================================
 */
public class HistorialTransferenciasView extends JFrame {

    private static final Color DORADO_PRINCIPAL = LoginView.DORADO_PRINCIPAL;
    private static final Color DORADO_OSCURO    = LoginView.DORADO_OSCURO;
    private static final Color GRIS_FONDO       = LoginView.GRIS_FONDO;
    private static final Color BLANCO           = LoginView.BLANCO;
    private static final Color TEXTO_OSCURO     = LoginView.TEXTO_OSCURO;
    private static final Color VERDE_TRANS      = new Color(0x00897B);
    private static final Color VERDE_EXITO      = new Color(0x27AE60);
    private static final Color ROJO_ERROR       = new Color(0xC0392B);
    private static final Color AMARILLO_PEND    = new Color(0xE0A100);
    private static final Color AZUL_PROC        = new Color(0x2980B9);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AutenticacionController  authController;
    private final TransferenciaController  transController;
    private final TransferenciasView       origen;

    private JTable            tabla;
    private DefaultTableModel modeloTabla;
    private int               filaHover = -1;
    private List<Transferencia> transferencias;

    public HistorialTransferenciasView(AutenticacionController authController,
                                       TransferenciaController transController,
                                       TransferenciasView origen) {
        this.authController  = authController;
        this.transController = transController;
        this.origen          = origen;
        initUI();
        cargarHistorial();
    }

    private void initUI() {
        setTitle("Qori Bank — Historial de Transferencias");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 680);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GRIS_FONDO);
        setContentPane(root);

        root.add(crearBarraSuperior(), BorderLayout.NORTH);
        root.add(crearPanelTabla(),    BorderLayout.CENTER);
    }

    // --------------------------------------------------------
    // Barra superior
    // --------------------------------------------------------
    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0,0,VERDE_TRANS,getWidth(),0,new Color(0x00695C)));
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        barra.setPreferredSize(new Dimension(0, 70));
        barra.setLayout(new BorderLayout());
        barra.setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        izq.setOpaque(false);
        JLabel lblTitulo = new JLabel("Historial de Transferencias");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(BLANCO);
        izq.add(lblTitulo);
        barra.add(izq, BorderLayout.CENTER);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        der.setOpaque(false);

        JButton btnActualizar = crearBotonBarra("⟳ Actualizar");
        btnActualizar.addActionListener(e -> cargarHistorial());
        der.add(btnActualizar);

        JButton btnVolver = crearBotonBarra("← Volver");
        btnVolver.addActionListener(e -> {
            origen.refrescarAlVolver();
            this.dispose();
        });
        der.add(btnVolver);
        barra.add(der, BorderLayout.EAST);
        return barra;
    }

    // --------------------------------------------------------
    // Panel de tabla
    // --------------------------------------------------------
    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(GRIS_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        String[] columnas = {
            "Fecha", "Tipo", "Tarjeta Origen", "Tarjeta Destino",
            "Entidad Destino", "Monto (S/)", "Estado", "Acción"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabla = new JTable(modeloTabla);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setRowHeight(42);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(0xEEEEEE));
        tabla.setBackground(BLANCO);
        tabla.setSelectionBackground(new Color(0xE0F2F1));

        JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        encabezado.setPreferredSize(new Dimension(0, 40));
        encabezado.setReorderingAllowed(false);
        encabezado.setDefaultRenderer(new HeaderRenderer());

        // Hover de fila
        tabla.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                int f = tabla.rowAtPoint(e.getPoint());
                if (f != filaHover) { filaHover = f; tabla.repaint(); }
                int c = tabla.columnAtPoint(e.getPoint());
                tabla.setCursor(Cursor.getPredefinedCursor(
                        (f >= 0 && c == 7) ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                filaHover = -1; tabla.repaint();
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int f = tabla.rowAtPoint(e.getPoint());
                int c = tabla.columnAtPoint(e.getPoint());
                if (f < 0 || c != 7) return;
                String accion = (String) modeloTabla.getValueAt(f, 7);
                if ("Descargar".equals(accion))  descargarComprobante(f);
                if ("Confirmar".equals(accion))  confirmarTransferencia(f);
            }
        });

        // Anchos de columnas
        int[] anchos = {130, 130, 140, 140, 130, 100, 100, 110};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        // Renderers
        for (int i : new int[]{0,1,2,3,4}) tabla.getColumnModel().getColumn(i).setCellRenderer(new HoverRenderer(SwingConstants.LEFT));
        tabla.getColumnModel().getColumn(5).setCellRenderer(new HoverRenderer(SwingConstants.RIGHT));
        tabla.getColumnModel().getColumn(6).setCellRenderer(new EstadoRenderer());
        tabla.getColumnModel().getColumn(7).setCellRenderer(new AccionRenderer());

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xDDDDDD)));
        scroll.getViewport().setBackground(BLANCO);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        // Leyenda de estados
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 4));
        leyenda.setOpaque(false);
        leyenda.add(crearLeyenda("EXITOSA", VERDE_EXITO));
        leyenda.add(crearLeyenda("PENDIENTE", AMARILLO_PEND));
        leyenda.add(crearLeyenda("PROCESANDO", AZUL_PROC));
        leyenda.add(crearLeyenda("RECHAZADA", ROJO_ERROR));
        panel.add(leyenda, BorderLayout.SOUTH);

        return panel;
    }

    // --------------------------------------------------------
    // Carga el historial en la tabla
    // --------------------------------------------------------
    private void cargarHistorial() {
        modeloTabla.setRowCount(0);
        transferencias = transController.obtenerHistorial();

        if (transferencias.isEmpty()) return;

        for (Transferencia t : transferencias) {
            String fecha    = t.getCreadoEn() != null ? t.getCreadoEn().format(FMT) : "-";
            String tipo     = t.getTipo() != null ? t.getTipo().name().replace("_", " ") : "-";
            boolean exitosa   = t.getEstado() == Transferencia.EstadoTransferencia.EXITOSA;
            boolean pendiente = t.getEstado() == Transferencia.EstadoTransferencia.PENDIENTE;

            String accion = exitosa   ? "Descargar"
                          : pendiente ? "Confirmar"
                          : "-";

            modeloTabla.addRow(new Object[]{
                fecha,
                tipo,
                t.getNumeroTarjetaOrigenEnmascarado(),
                t.getNumeroTarjetaDestinoEnmascarado(),
                t.getEntidadDestino(),
                String.format("%,.2f", t.getMonto()),
                t.getEstado().name(),
                accion
            });
        }
    }

    // --------------------------------------------------------
    // Descarga el comprobante de la transferencia seleccionada
    // --------------------------------------------------------
    private void descargarComprobante(int fila) {
        if (transferencias == null || fila >= transferencias.size()) return;
        Transferencia t = transferencias.get(fila);

        if (t.getEstado() != Transferencia.EstadoTransferencia.EXITOSA) {
            JOptionPane.showMessageDialog(this,
                    "Solo se puede descargar el comprobante de transferencias exitosas.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Elegir ruta con JFileChooser
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("comprobante_" + t.getIdTransferencia() + ".txt"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        try (FileWriter fw = new FileWriter(chooser.getSelectedFile())) {
            Usuario u = authController.getUsuarioActual();
            String sep = "================================================";

            fw.write(sep + "\n");
            fw.write("       QORI BANK — COMPROBANTE DE TRANSFERENCIA\n");
            fw.write(sep + "\n\n");
            fw.write("N° Operacion  : " + t.getIdTransferencia() + "\n");
            fw.write("Fecha         : " + (t.getProcesadoEn() != null ? t.getProcesadoEn().format(FMT) : "-") + "\n");
            fw.write("Estado        : " + t.getEstado().name() + "\n\n");
            fw.write(sep + "\n");
            fw.write("DATOS DEL REMITENTE\n");
            fw.write(sep + "\n");
            fw.write("Nombre        : " + (u != null ? u.getNombreCompleto() : "-") + "\n");
            fw.write("Tarjeta Origen: " + t.getNumeroTarjetaOrigenEnmascarado() + "\n\n");
            fw.write(sep + "\n");
            fw.write("DATOS DEL DESTINATARIO\n");
            fw.write(sep + "\n");
            fw.write("Tarjeta Dest. : " + t.getNumeroTarjetaDestinoEnmascarado() + "\n");
            fw.write("Entidad Dest. : " + t.getEntidadDestino() + "\n");
            fw.write("Tipo          : " + (t.getTipo() != null ? t.getTipo().name().replace("_"," ") : "-") + "\n\n");
            fw.write(sep + "\n");
            fw.write("DETALLE\n");
            fw.write(sep + "\n");
            fw.write("Monto         : S/ " + String.format("%,.2f", t.getMonto()) + "\n");
            fw.write("Descripcion   : " + (t.getDescripcion() != null ? t.getDescripcion() : "-") + "\n\n");
            fw.write(sep + "\n");
            fw.write("  Este documento es un comprobante digital generado\n");
            fw.write("  automaticamente por Qori Bank.\n");
            fw.write(sep + "\n");

            JOptionPane.showMessageDialog(this,
                    "Comprobante descargado correctamente.",
                    "Descarga exitosa", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar el comprobante: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --------------------------------------------------------
    // Confirmar transferencia pendiente (alerta de seguridad)
    // --------------------------------------------------------
    private void confirmarTransferencia(int fila) {
        if (transferencias == null || fila >= transferencias.size()) return;
        Transferencia t = transferencias.get(fila);

        if (t.getEstado() != Transferencia.EstadoTransferencia.PENDIENTE) {
            JOptionPane.showMessageDialog(this,
                    "Solo se pueden confirmar transferencias en estado PENDIENTE.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Elegir canal para el OTP
        String[] opciones = {"SMS", "Correo electrónico"};
        int opCanal = JOptionPane.showOptionDialog(this,
                "¿Por qué canal desea recibir el código de confirmación?",
                "Confirmar transferencia de S/ " + String.format("%,.2f", t.getMonto()),
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opciones, opciones[0]);
        if (opCanal < 0) return;

        InicioSesion.CanalVerificacion canal = (opCanal == 0)
                ? InicioSesion.CanalVerificacion.SMS
                : InicioSesion.CanalVerificacion.CORREO;

        // Enviar código OTP para la alerta
        NotificacionController notifCtrl =
                new NotificacionController(authController.getUsuarioActual());

        // Obtener la notificación de alerta vinculada a esta transferencia
        java.util.List<model.Notificacion> notifs = notifCtrl.obtenerNotificaciones();
        model.Notificacion alerta = notifs.stream()
                .filter(n -> n.getTipo() == model.Notificacion.TipoNotificacion.ALERTA
                          && n.getIdTransferencia() != null
                          && n.getIdTransferencia() == t.getIdTransferencia()
                          && !n.isLeida())
                .findFirst()
                .orElse(null);

        if (alerta == null) {
            JOptionPane.showMessageDialog(this,
                    "No se encontró la alerta asociada a esta transferencia.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String errorEnvio = notifCtrl.enviarCodigoAlerta(alerta, canal);
        if (errorEnvio != null) {
            JOptionPane.showMessageDialog(this, errorEnvio, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Diálogo para ingresar el código
        JPanel panelCodigo = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.weightx = 1.0;

        JLabel lblInfo = new JLabel(
                "<html>Ingrese el código enviado por <b>" + canal.getNombre() + "</b>.<br>"
                + "<b>Simulación: " + notifCtrl.getCodigoGeneradoSimulacion() + "</b></html>");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.gridy = 0; g.insets = new Insets(0, 0, 12, 0);
        panelCodigo.add(lblInfo, g);

        JTextField txtCodigo = new JTextField();
        txtCodigo.setPreferredSize(new Dimension(200, 42));
        txtCodigo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        txtCodigo.setHorizontalAlignment(JTextField.CENTER);
        // Solo dígitos, máx 6
        txtCodigo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != java.awt.event.KeyEvent.VK_BACK_SPACE)
                    e.consume();
                if (txtCodigo.getText().length() >= 6
                        && c != java.awt.event.KeyEvent.VK_BACK_SPACE)
                    e.consume();
            }
        });
        g.gridy = 1; g.insets = new Insets(0, 0, 0, 0);
        panelCodigo.add(txtCodigo, g);

        int resultado = JOptionPane.showConfirmDialog(this, panelCodigo,
                "Código de confirmación",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado != JOptionPane.OK_OPTION) return;

        String errorConfirm = notifCtrl.confirmarAlerta(txtCodigo.getText(), canal);
        if (errorConfirm != null) {
            JOptionPane.showMessageDialog(this, errorConfirm, "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "<html><b>Transferencia confirmada exitosamente.</b><br>"
                    + "Monto: S/ " + String.format("%,.2f", t.getMonto()) + "<br>"
                    + "Destino: " + t.getNumeroTarjetaDestinoEnmascarado()
                    + " (" + t.getEntidadDestino() + ")</html>",
                    "Transferencia procesada", JOptionPane.INFORMATION_MESSAGE);
            cargarHistorial();
        }
    }

    // ============================================================
    // Renderers
    // ============================================================

    private Color colorFila(boolean isSelected, int row) {
        if (isSelected) return tabla.getSelectionBackground();
        if (row == filaHover) return new Color(0xF5F5F5);
        return BLANCO;
    }

    private class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setOpaque(true);
        }
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,v,sel,foc,r,c);
            lbl.setBackground(VERDE_TRANS);
            lbl.setForeground(BLANCO);
            lbl.setBorder(BorderFactory.createMatteBorder(0,0,1,1,new Color(0x00695C)));
            return lbl;
        }
    }

    private class HoverRenderer extends DefaultTableCellRenderer {
        HoverRenderer(int align) { setHorizontalAlignment(align); }
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,v,sel,foc,r,c);
            lbl.setOpaque(true);
            lbl.setForeground(TEXTO_OSCURO);
            lbl.setBackground(colorFila(sel, r));
            return lbl;
        }
    }

    private class EstadoRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,v,sel,foc,r,c);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setOpaque(true);
            if (!sel) {
                String estado = v != null ? v.toString() : "";
                switch (estado) {
                    case "EXITOSA":     lbl.setForeground(VERDE_EXITO);   break;
                    case "RECHAZADA":   lbl.setForeground(ROJO_ERROR);    break;
                    case "PROCESANDO":  lbl.setForeground(AZUL_PROC);     break;
                    default:            lbl.setForeground(AMARILLO_PEND); break;
                }
                lbl.setBackground(colorFila(false, r));
            }
            return lbl;
        }
    }

    /**
     * Renderer unificado para la columna Acción:
     * - "Descargar" → botón verde
     * - "Confirmar" → botón naranja/dorado
     * - "-"         → guión gris
     */
    private class AccionRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            Color fondo = colorFila(sel, r);
            String val  = v != null ? v.toString() : "-";

            if ("-".equals(val)) {
                JLabel lbl = new JLabel("-");
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setForeground(Color.GRAY);
                lbl.setOpaque(true);
                lbl.setBackground(fondo);
                return lbl;
            }

            boolean esConfirmar = "Confirmar".equals(val);
            Color colorBtn = esConfirmar ? new Color(0xE67E22) : VERDE_TRANS;

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            panel.setBackground(fondo);

            JButton btn = new JButton(val) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(colorBtn);
                    g2.fill(new RoundRectangle2D.Float(
                            2, 4, getWidth()-4, getHeight()-8, 8, 8));
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
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            panel.add(btn);
            return panel;
        }
    }

    // --------------------------------------------------------
    // Helpers de UI
    // --------------------------------------------------------
    private JLabel crearLeyenda(String texto, Color color) {
        JLabel lbl = new JLabel("  " + texto + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),getHeight(),getHeight()));
                g2.dispose(); super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(BLANCO); lbl.setOpaque(false);
        return lbl;
    }

    private JButton crearBotonBarra(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(VERDE_TRANS);
        btn.setBackground(BLANCO);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BLANCO, 1, true),
                BorderFactory.createEmptyBorder(5,14,5,14)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }
}
