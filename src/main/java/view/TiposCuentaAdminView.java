package view;

import controller.CuentaController;
import model.TipoCuentaBancaria;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * ============================================================
 * CAPA: VIEW — Vista de Administración de Bancos
 * ============================================================
 * Solo accesible para usuarios con rol ADMIN (se llega desde
 * AdminView). Permite:
 *   - Ver todas las entidades bancarias disponibles (BCP, BBVA,
 *     Interbank, y cualquier otra agregada dinámicamente)
 *   - Crear un nuevo tipo de cuenta bancaria (nombre + API)
 *   - Eliminar (o desactivar, si ya tiene cuentas vinculadas)
 *
 * Clase: TiposCuentaAdminView
 * Módulo: Vinculación de Cuentas Bancarias — QoriBank
 * ============================================================
 */
public class TiposCuentaAdminView extends JFrame {

    private static final Color DORADO_PRINCIPAL = LoginView.DORADO_PRINCIPAL;
    private static final Color DORADO_OSCURO    = LoginView.DORADO_OSCURO;
    private static final Color GRIS_FONDO       = LoginView.GRIS_FONDO;
    private static final Color BLANCO           = LoginView.BLANCO;
    private static final Color TEXTO_OSCURO     = LoginView.TEXTO_OSCURO;
    private static final Color GRIS_CAMPO       = LoginView.GRIS_CAMPO;
    private static final Color ROJO_ERROR       = LoginView.ROJO_ERROR;
    private static final Color VERDE_EXITO      = new Color(0x27AE60);
    private static final Color MORADO_CUENTAS   = new Color(0x6C5CE7);
    private static final Color ROJO_ELIMINAR    = new Color(0xC0392B);

    private final AdminView origen;
    private final CuentaController cuentaController;

    private JTable            tablaBancos;
    private DefaultTableModel modeloTabla;
    private int                filaHover = -1;

    private JTextField   txtNombreEntidad;
    private JTextField   txtApiEndpoint;
    private JTextField   txtApiKey;
    private JLabel        lblFormError;
    private JLabel        lblFormExito;

    public TiposCuentaAdminView(AdminView origen) {
        this.origen           = origen;
        this.cuentaController = new CuentaController(null); // operaciones de admin no requieren usuarioActual
        initUI();
        cargarBancos();
    }

    private void initUI() {
        setTitle("Qori Bank — Administración de Bancos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 680);
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
                        0, 0, MORADO_CUENTAS, getWidth(), 0, new Color(0x4834A0));
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
        JLabel lblIcono = new JLabel("\uD83C\uDFE6");
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        JLabel lblTitulo = new JLabel("Administración de Bancos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(BLANCO);
        izq.add(lblIcono);
        izq.add(lblTitulo);
        barra.add(izq, BorderLayout.CENTER);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        der.setOpaque(false);
        JButton btnVolver = crearBotonBarra("← Volver al panel Admin");
        btnVolver.addActionListener(e -> {
            origen.setVisible(true);
            this.dispose();
        });
        der.add(btnVolver);
        barra.add(der, BorderLayout.EAST);

        return barra;
    }

    // --------------------------------------------------------
    // Panel central: tabla izquierda + formulario derecha
    // --------------------------------------------------------
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(GRIS_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.insets  = new Insets(0, 0, 0, 16);
        gbc.weighty = 1.0;

        gbc.gridx   = 0;
        gbc.weightx = 0.62;
        panel.add(crearPanelTabla(), gbc);

        gbc.gridx   = 1;
        gbc.weightx = 0.38;
        gbc.insets  = new Insets(0, 0, 0, 0);
        panel.add(crearPanelFormulario(), gbc);

        return panel;
    }

    // --------------------------------------------------------
    // Panel izquierdo: tabla de bancos
    // --------------------------------------------------------
    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitulo = new JLabel("Entidades Bancarias");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(TEXTO_OSCURO);
        header.add(lblTitulo, BorderLayout.WEST);

        JButton btnRefresh = new JButton("⟳ Actualizar");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setForeground(MORADO_CUENTAS);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setContentAreaFilled(false);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> cargarBancos());
        header.add(btnRefresh, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        String[] columnas = {"ID", "Entidad", "API Endpoint", "API Key", "Acción"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tablaBancos = new JTable(modeloTabla);
        tablaBancos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaBancos.setRowHeight(40);
        tablaBancos.setShowVerticalLines(false);
        tablaBancos.setGridColor(new Color(0xEEEEEE));
        tablaBancos.setBackground(BLANCO);
        tablaBancos.setSelectionBackground(new Color(0xEDE7F6));

        JTableHeader encabezado = tablaBancos.getTableHeader();
        encabezado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        encabezado.setPreferredSize(new Dimension(0, 38));
        encabezado.setReorderingAllowed(false);
        encabezado.setDefaultRenderer(new HeaderRenderer());

        tablaBancos.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int fila = tablaBancos.rowAtPoint(e.getPoint());
                if (fila != filaHover) {
                    filaHover = fila;
                    tablaBancos.repaint();
                }
                int columna = tablaBancos.columnAtPoint(e.getPoint());
                boolean sobreEliminar = fila >= 0 && columna == 4;
                tablaBancos.setCursor(Cursor.getPredefinedCursor(
                        sobreEliminar ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
        tablaBancos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                filaHover = -1;
                tablaBancos.repaint();
            }
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int columna = tablaBancos.columnAtPoint(e.getPoint());
                int fila    = tablaBancos.rowAtPoint(e.getPoint());
                if (fila < 0 || columna != 4) return;
                eliminarBanco(fila);
            }
        });

        int[] anchos = {40, 140, 220, 160, 90};
        for (int i = 0; i < anchos.length; i++)
            tablaBancos.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        tablaBancos.getColumnModel().getColumn(0).setCellRenderer(new HoverRenderer(SwingConstants.CENTER));
        tablaBancos.getColumnModel().getColumn(1).setCellRenderer(new HoverRenderer(SwingConstants.LEFT));
        tablaBancos.getColumnModel().getColumn(2).setCellRenderer(new HoverRenderer(SwingConstants.LEFT));
        tablaBancos.getColumnModel().getColumn(3).setCellRenderer(new HoverRenderer(SwingConstants.LEFT));
        tablaBancos.getColumnModel().getColumn(4).setCellRenderer(new EliminarRenderer());

        JScrollPane scroll = new JScrollPane(tablaBancos);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xDDDDDD)));
        scroll.getViewport().setBackground(BLANCO);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // --------------------------------------------------------
    // Panel derecho: formulario Crear Entidad Bancaria
    // --------------------------------------------------------
    private JPanel crearPanelFormulario() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLANCO);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(MORADO_CUENTAS);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 6, 6, 6));
                g2.dispose();
            }
        };
        card.setLayout(new GridBagLayout());
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.gridx   = 0;
        gbc.weightx = 1.0;

        JLabel lblTitulo = new JLabel("Nueva Entidad Bancaria");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(TEXTO_OSCURO);
        gbc.gridy  = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblTitulo, gbc);

        JLabel lblSub = new JLabel("<html>Se creará a partir de una plantilla base<br>(PATRÓN PROTOTYPE).</html>");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.GRAY);
        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 0, 14, 0);
        card.add(lblSub, gbc);

        JLabel lblNombre = new JLabel("Nombre de la entidad *");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblNombre.setForeground(TEXTO_OSCURO);
        gbc.gridy  = 2;
        gbc.insets = new Insets(6, 0, 2, 0);
        card.add(lblNombre, gbc);
        txtNombreEntidad = crearCampoForm();
        gbc.gridy  = 3;
        gbc.insets = new Insets(0, 0, 2, 0);
        card.add(txtNombreEntidad, gbc);

        JLabel lblEndpoint = new JLabel("API Endpoint *");
        lblEndpoint.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblEndpoint.setForeground(TEXTO_OSCURO);
        gbc.gridy  = 4;
        gbc.insets = new Insets(6, 0, 2, 0);
        card.add(lblEndpoint, gbc);
        txtApiEndpoint = crearCampoForm();
        gbc.gridy  = 5;
        gbc.insets = new Insets(0, 0, 2, 0);
        card.add(txtApiEndpoint, gbc);

        JLabel lblApiKey = new JLabel("API Key *");
        lblApiKey.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblApiKey.setForeground(TEXTO_OSCURO);
        gbc.gridy  = 6;
        gbc.insets = new Insets(6, 0, 2, 0);
        card.add(lblApiKey, gbc);
        txtApiKey = crearCampoForm();
        gbc.gridy  = 7;
        gbc.insets = new Insets(0, 0, 2, 0);
        card.add(txtApiKey, gbc);

        lblFormError = new JLabel("");
        lblFormError.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFormError.setForeground(ROJO_ERROR);
        lblFormError.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy  = 8;
        gbc.insets = new Insets(6, 0, 2, 0);
        card.add(lblFormError, gbc);

        lblFormExito = new JLabel("");
        lblFormExito.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblFormExito.setForeground(VERDE_EXITO);
        lblFormExito.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy  = 9;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblFormExito, gbc);

        JButton btnCrear = crearBotonAccion("Crear Entidad Bancaria", MORADO_CUENTAS);
        btnCrear.addActionListener(e -> procesarCrearBanco());
        gbc.gridy  = 10;
        gbc.insets = new Insets(4, 0, 0, 0);
        card.add(btnCrear, gbc);

        return card;
    }

    // --------------------------------------------------------
    // Carga la lista de entidades bancarias en la tabla
    // --------------------------------------------------------
    private void cargarBancos() {
        modeloTabla.setRowCount(0);
        List<TipoCuentaBancaria> bancos = cuentaController.obtenerTiposCuentaDisponibles();

        for (TipoCuentaBancaria t : bancos) {
            modeloTabla.addRow(new Object[]{
                t.getIdTipoCuenta(),
                t.getNombreEntidad(),
                t.getApiEndpoint(),
                t.getApiKey(),
                "Eliminar"
            });
        }
    }

    // --------------------------------------------------------
    // Evento: Crear entidad bancaria
    // --------------------------------------------------------
    private void procesarCrearBanco() {
        lblFormError.setText("");
        lblFormExito.setText("");

        String error = cuentaController.crearTipoCuentaBanco(
                txtNombreEntidad.getText(), txtApiEndpoint.getText(), txtApiKey.getText());

        if (error != null) {
            lblFormError.setText(error);
            return;
        }

        lblFormExito.setText("✅ Entidad bancaria creada correctamente.");
        txtNombreEntidad.setText("");
        txtApiEndpoint.setText("");
        txtApiKey.setText("");
        cargarBancos();
    }

    // --------------------------------------------------------
    // Acción: Eliminar entidad bancaria desde la tabla
    // --------------------------------------------------------
    private void eliminarBanco(int fila) {
        int idTipoCuenta = (int) modeloTabla.getValueAt(fila, 0);
        String nombre     = (String) modeloTabla.getValueAt(fila, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la entidad bancaria \"" + nombre + "\"?\n"
                + "Si ya tiene cuentas vinculadas, solo se desactivará.",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        String error = cuentaController.eliminarTipoCuentaBanco(idTipoCuenta);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Entidad bancaria eliminada/desactivada correctamente.",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
        cargarBancos();
    }

    // ============================================================
    // Renderers
    // ============================================================

    private Color colorFilaFondo(JTable table, boolean isSelected, int row) {
        if (isSelected) return table.getSelectionBackground();
        if (row == filaHover) return new Color(0xF5F5F5);
        return BLANCO;
    }

    private class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            lbl.setBackground(MORADO_CUENTAS);
            lbl.setForeground(BLANCO);
            lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(0x4834A0)));
            return lbl;
        }
    }

    private class HoverRenderer extends DefaultTableCellRenderer {
        HoverRenderer(int alineacion) {
            setHorizontalAlignment(alineacion);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            lbl.setOpaque(true);
            lbl.setForeground(TEXTO_OSCURO);
            lbl.setBackground(colorFilaFondo(table, isSelected, row));
            return lbl;
        }
    }

    private class EliminarRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Color fondo = colorFilaFondo(table, isSelected, row);
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            panel.setBackground(fondo);
            panel.add(crearBotonEliminarCelda());
            return panel;
        }
    }

    private JButton crearBotonEliminarCelda() {
        JButton btn = new JButton("Eliminar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ROJO_ELIMINAR);
                g2.fill(new RoundRectangle2D.Float(2, 4, getWidth() - 4, getHeight() - 8, 8, 8));
                g2.setColor(BLANCO);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        return btn;
    }

    // --------------------------------------------------------
    // Helpers de UI
    // --------------------------------------------------------
    private JTextField crearCampoForm() {
        JTextField campo = new JTextField();
        campo.setPreferredSize(new Dimension(0, 34));
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        campo.setBackground(GRIS_CAMPO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCCCCCC), 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        campo.setOpaque(true);
        return campo;
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
        btn.setPreferredSize(new Dimension(0, 40));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton crearBotonBarra(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(BLANCO);
        btn.setBackground(MORADO_CUENTAS);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BLANCO, 1, true),
                BorderFactory.createEmptyBorder(5, 14, 5, 14)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }
}
