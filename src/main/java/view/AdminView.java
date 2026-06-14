package view;

import controller.AutenticacionController;
import model.Usuario;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * ============================================================
 * CAPA: VIEW — Vista de Panel de Administración
 * ============================================================
 * Solo accesible para usuarios con rol ADMIN.
 * Permite:
 *   - Ver todos los usuarios (tabla con nombre, email,
 *     teléfono, rol y estado)
 *   - Eliminar cualquier usuario (excepto a sí mismo)
 *   - Crear nuevos administradores
 *
 * Clase: AdminView
 * Módulo: Gestión de Usuarios y Autenticación — QoriBank
 * ============================================================
 */
public class AdminView extends JFrame {

    private static final Color DORADO_PRINCIPAL = LoginView.DORADO_PRINCIPAL;
    private static final Color DORADO_OSCURO    = LoginView.DORADO_OSCURO;
    private static final Color GRIS_FONDO       = LoginView.GRIS_FONDO;
    private static final Color BLANCO           = LoginView.BLANCO;
    private static final Color TEXTO_OSCURO     = LoginView.TEXTO_OSCURO;
    private static final Color GRIS_CAMPO       = LoginView.GRIS_CAMPO;
    private static final Color ROJO_ERROR       = LoginView.ROJO_ERROR;
    private static final Color VERDE_EXITO      = new Color(0x27AE60);
    private static final Color AZUL_ADMIN       = new Color(0x2980B9);
    private static final Color ROJO_ELIMINAR    = new Color(0xC0392B);

    // Tabla
    private JTable           tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private int              filaHover = -1;

    // Formulario Crear Admin
    private JTextField    txtNombre;
    private JTextField    txtApellidoP;
    private JTextField    txtApellidoM;
    private JTextField    txtEmail;
    private JTextField    txtTelefono;
    private JPasswordField txtPassword;
    private JLabel        lblFormError;
    private JLabel        lblFormExito;

    private final AutenticacionController controller;

    public AdminView(AutenticacionController controller) {
        this.controller = controller;
        initUI();
        cargarUsuarios();
    }

    // --------------------------------------------------------
    // Construcción principal
    // --------------------------------------------------------
    private void initUI() {
        setTitle("Qori Bank — Panel de Administración");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GRIS_FONDO);
        setContentPane(root);

        root.add(crearBarraSuperior(),  BorderLayout.NORTH);
        root.add(crearPanelCentral(),   BorderLayout.CENTER);
    }

    // --------------------------------------------------------
    // Barra superior (cabecera de navegación)
    // --------------------------------------------------------
    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint grad = new GradientPaint(
                        0, 0, DORADO_PRINCIPAL,
                        getWidth(), 0, DORADO_OSCURO);
                g2.setPaint(grad);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        barra.setPreferredSize(new Dimension(0, 70));
        barra.setMinimumSize(new Dimension(0, 70));
        barra.setLayout(new BorderLayout());
        barra.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));

        // Título izquierda
        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        izq.setOpaque(false);

        JLabel lblIcono = new JLabel("⚙");
        lblIcono.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24));
        lblIcono.setForeground(BLANCO);

        JLabel lblTitulo = new JLabel("Panel de Administración");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(BLANCO);

        izq.add(lblIcono);
        izq.add(lblTitulo);
        barra.add(izq, BorderLayout.CENTER);

        // Usuario actual + botones derecha
        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        der.setOpaque(false);

        Usuario u = controller.getUsuarioActual();
        JLabel lblUser = new JLabel("Usuario: " + u.getNombreCompleto() + "  |  ADMIN");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUser.setForeground(BLANCO);
        der.add(lblUser);

        JButton btnVolver = crearBotonBarra("← Volver", BLANCO, DORADO_OSCURO);
        btnVolver.addActionListener(e -> {
            new BienvenidaView(controller).setVisible(true);
            this.dispose();
        });

        JButton btnCerrar = crearBotonBarra("Cerrar Sesión", BLANCO, DORADO_OSCURO);
        btnCerrar.addActionListener(e -> {
            controller.cerrarSesion();
            new LoginView().setVisible(true);
            this.dispose();
        });

        der.add(btnVolver);
        der.add(btnCerrar);
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
        gbc.fill   = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 16);
        gbc.weighty = 1.0;

        // Tabla de usuarios (lado izquierdo, 65% del ancho)
        gbc.gridx   = 0;
        gbc.weightx = 0.65;
        panel.add(crearPanelTabla(), gbc);

        // Formulario crear admin (lado derecho, 35%)
        gbc.gridx   = 1;
        gbc.weightx = 0.35;
        gbc.insets  = new Insets(0, 0, 0, 0);
        panel.add(crearPanelFormulario(), gbc);

        return panel;
    }

    // --------------------------------------------------------
    // Panel izquierdo: tabla de usuarios
    // --------------------------------------------------------
    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Encabezado + botón Refrescar
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitulo = new JLabel("Usuarios del Sistema");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(TEXTO_OSCURO);
        lblTitulo.setMinimumSize(new Dimension(200, 24));
        header.add(lblTitulo, BorderLayout.WEST);

        JButton btnRefresh = new JButton("⟳ Actualizar");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setForeground(AZUL_ADMIN);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setContentAreaFilled(false);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> cargarUsuarios());
        header.add(btnRefresh, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        // Columnas de la tabla
        String[] columnas = {"ID", "Nombre Completo", "Correo", "Teléfono", "Rol", "Activo", "Acción"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaUsuarios.setRowHeight(40);
        tablaUsuarios.setShowVerticalLines(false);
        tablaUsuarios.setGridColor(new Color(0xEEEEEE));
        tablaUsuarios.setBackground(BLANCO);
        tablaUsuarios.setSelectionBackground(new Color(0xFFF3CD));

        // Encabezado de la tabla: renderer custom (naranja + texto blanco)
        JTableHeader encabezado = tablaUsuarios.getTableHeader();
        encabezado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        encabezado.setPreferredSize(new Dimension(0, 38));
        encabezado.setReorderingAllowed(false);
        encabezado.setDefaultRenderer(new HeaderRenderer());

        // Resaltado de fila al pasar el mouse (gris muy suave)
        tablaUsuarios.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int fila = tablaUsuarios.rowAtPoint(e.getPoint());
                if (fila != filaHover) {
                    filaHover = fila;
                    tablaUsuarios.repaint();
                }
            }
        });
        tablaUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                filaHover = -1;
                tablaUsuarios.repaint();
            }
        });

        // Anchos de columnas
        int[] anchos = {40, 180, 200, 120, 80, 70, 110};
        for (int i = 0; i < anchos.length; i++)
            tablaUsuarios.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        // Renderer centrado para algunas columnas
        for (int col : new int[]{0, 5})
            tablaUsuarios.getColumnModel().getColumn(col).setCellRenderer(new HoverRenderer(SwingConstants.CENTER));

        for (int col : new int[]{1, 2, 3})
            tablaUsuarios.getColumnModel().getColumn(col).setCellRenderer(new HoverRenderer(SwingConstants.LEFT));

        // Renderer para columna "Rol" con color
        tablaUsuarios.getColumnModel().getColumn(4).setCellRenderer(new RolRenderer());

        // Renderer para columna "Acción" (botón visual Eliminar)
        tablaUsuarios.getColumnModel().getColumn(6).setCellRenderer(new EliminarRenderer());

        // Detectar clic en el botón "Eliminar" (un solo clic, sin editor)
        tablaUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int columna = tablaUsuarios.columnAtPoint(e.getPoint());
                int fila    = tablaUsuarios.rowAtPoint(e.getPoint());
                if (fila < 0 || columna != 6) return;

                Object valor = modeloTabla.getValueAt(fila, 6);
                if ("Eliminar".equals(valor)) {
                    eliminarUsuario(fila);
                }
            }
        });

        // Cursor de mano sobre el botón "Eliminar"
        tablaUsuarios.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int fila    = tablaUsuarios.rowAtPoint(e.getPoint());
                int columna = tablaUsuarios.columnAtPoint(e.getPoint());
                boolean sobreEliminar = fila >= 0 && columna == 6
                        && "Eliminar".equals(modeloTabla.getValueAt(fila, 6));
                tablaUsuarios.setCursor(Cursor.getPredefinedCursor(
                        sobreEliminar ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        JScrollPane scroll = new JScrollPane(tablaUsuarios);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xDDDDDD)));
        scroll.getViewport().setBackground(BLANCO);
        panel.add(scroll, BorderLayout.CENTER);


        return panel;
    }

    // --------------------------------------------------------
    // Panel derecho: formulario Crear Administrador
    // --------------------------------------------------------
    private JPanel crearPanelFormulario() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLANCO);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                // Franja azul superior
                g2.setColor(AZUL_ADMIN);
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

        JLabel lblTitulo = new JLabel("Crear Administrador");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(TEXTO_OSCURO);
        gbc.gridy  = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblTitulo, gbc);

        JLabel lblSub = new JLabel("Solo se crean cuentas con rol ADMIN.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.GRAY);
        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 0, 14, 0);
        card.add(lblSub, gbc);

        // Campos del formulario
        Object[][] campos = {
            {"Primer Nombre *",        null},
            {"Apellido Paterno *",     null},
            {"Apellido Materno",       null},
            {"Correo Electrónico *",   null},
            {"Teléfono *",             null},
            {"Contraseña * (mín. 6)", "pwd"}
        };

        JTextField[] refs = new JTextField[5];
        int fila = 2;
        int refIdx = 0;

        for (Object[] campo : campos) {
            String label   = (String) campo[0];
            boolean esPwd  = "pwd".equals(campo[1]);

            JLabel lbl = new JLabel(label);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(TEXTO_OSCURO);
            gbc.gridy  = fila++;
            gbc.insets = new Insets(6, 0, 2, 0);
            card.add(lbl, gbc);

            JComponent input;
            if (esPwd) {
                txtPassword = new JPasswordField();
                estilizarCampoForm(txtPassword);
                input = txtPassword;
            } else {
                JTextField tf = new JTextField();
                estilizarCampoForm(tf);
                refs[refIdx++] = tf;
                input = tf;
            }

            gbc.gridy  = fila++;
            gbc.insets = new Insets(0, 0, 2, 0);
            card.add(input, gbc);
        }

        // Asignar referencias por orden
        txtNombre    = refs[0];
        txtApellidoP = refs[1];
        txtApellidoM = refs[2];
        txtEmail     = refs[3];
        txtTelefono  = refs[4];

        // Mensajes de feedback
        lblFormError = new JLabel("");
        lblFormError.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFormError.setForeground(ROJO_ERROR);
        lblFormError.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy  = fila++;
        gbc.insets = new Insets(6, 0, 2, 0);
        card.add(lblFormError, gbc);

        lblFormExito = new JLabel("");
        lblFormExito.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblFormExito.setForeground(VERDE_EXITO);
        lblFormExito.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy  = fila++;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblFormExito, gbc);

        // Botón Crear Admin
        JButton btnCrear = crearBotonAccion("Crear Administrador", AZUL_ADMIN);
        btnCrear.addActionListener(e -> procesarCrearAdmin());
        gbc.gridy  = fila++;
        gbc.insets = new Insets(4, 0, 0, 0);
        card.add(btnCrear, gbc);

        return card;
    }

    // --------------------------------------------------------
    // Carga la lista de usuarios en la tabla
    // --------------------------------------------------------
    private void cargarUsuarios() {
        modeloTabla.setRowCount(0);
        List<Usuario> usuarios = controller.obtenerTodosUsuarios();
        Usuario yo = controller.getUsuarioActual();

        for (Usuario u : usuarios) {
            modeloTabla.addRow(new Object[]{
                u.getIdUsuario(),
                u.getNombreCompleto(),
                u.getEmail(),
                u.getTelefono(),
                u.getRol(),
                u.isActivo() ? "Sí" : "No",
                // No mostrar "Eliminar" para el usuario actual (no puede autoeliminarse)
                (yo != null && yo.getIdUsuario() == u.getIdUsuario()) ? "—" : "Eliminar"
            });
        }
    }

    // --------------------------------------------------------
    // Evento: Crear administrador
    // --------------------------------------------------------
    private void procesarCrearAdmin() {
        lblFormError.setText("");
        lblFormExito.setText("");

        String password = new String(txtPassword.getPassword());

        String error = controller.crearAdmin(
                txtNombre.getText(),
                txtApellidoP.getText(),
                txtApellidoM.getText(),
                txtEmail.getText(),
                txtTelefono.getText(),
                password
        );

        if (error != null) {
            lblFormError.setText(error);
            return;
        }

        lblFormExito.setText("✅ Administrador creado correctamente.");
        limpiarFormulario();
        cargarUsuarios();   // Refrescar tabla
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtApellidoP.setText("");
        txtApellidoM.setText("");
        txtEmail.setText("");
        txtTelefono.setText("");
        txtPassword.setText("");
    }

    // --------------------------------------------------------
    // Acción: Eliminar usuario desde la tabla
    // --------------------------------------------------------
    private void eliminarUsuario(int fila) {
        int idUsuario = (int) modeloTabla.getValueAt(fila, 0);
        String nombre = (String) modeloTabla.getValueAt(fila, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar al usuario \"" + nombre + "\"?\nEsta acción no se puede deshacer.",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        String error = controller.eliminarUsuario(idUsuario);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Usuario eliminado correctamente.",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
        cargarUsuarios();
    }

    // ============================================================
    // Renderers y Editors para la tabla
    // ============================================================

    /** Color de fondo para una fila, según hover/selección */
    private Color colorFilaFondo(JTable table, boolean isSelected, int row) {
        if (isSelected) return table.getSelectionBackground();
        if (row == filaHover) return new Color(0xF5F5F5); // gris muy suave
        return BLANCO;
    }

    /** Encabezado de tabla: fondo naranja, texto blanco, centrado */
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
            lbl.setBackground(DORADO_PRINCIPAL);
            lbl.setForeground(BLANCO);
            lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, DORADO_OSCURO));
            return lbl;
        }
    }

    /** Renderer genérico de celda con soporte de hover gris suave */
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

    /** Colorea la celda de "Rol" según su valor, con soporte hover */
    private class RolRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setOpaque(true);

            if (isSelected) {
                lbl.setBackground(table.getSelectionBackground());
                lbl.setForeground(TEXTO_OSCURO);
                return lbl;
            }

            String rol = value != null ? value.toString() : "";
            if ("ADMIN".equals(rol)) {
                lbl.setForeground(AZUL_ADMIN);
            } else {
                lbl.setForeground(new Color(0x7F8C8D));
            }
            lbl.setBackground(colorFilaFondo(table, false, row));
            return lbl;
        }
    }

    /** Renderiza el botón "Eliminar" en la columna de acción, con fondo de fila (hover) */
    private class EliminarRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Color fondo = colorFilaFondo(table, isSelected, row);
            String val = value != null ? value.toString() : "";

            if ("—".equals(val)) {
                JLabel lbl = new JLabel("—");
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                lbl.setForeground(Color.GRAY);
                lbl.setOpaque(true);
                lbl.setBackground(fondo);
                return lbl;
            }

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            panel.setBackground(fondo);
            panel.add(crearBotonEliminarCelda());
            return panel;
        }
    }


    /** Crea un botón "Eliminar" rojo, custom-pintado, para usarlo dentro de la tabla */
    private JButton crearBotonEliminarCelda() {
        JButton btn = new JButton("Eliminar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() || getModel().isPressed()
                        ? ROJO_ELIMINAR.darker() : ROJO_ELIMINAR);
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
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // --------------------------------------------------------
    // Helpers de UI
    // --------------------------------------------------------
    private void estilizarCampoForm(JComponent campo) {
        campo.setPreferredSize(new Dimension(0, 34));
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        campo.setBackground(GRIS_CAMPO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCCCCCC), 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        campo.setOpaque(true);
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

    private JButton crearBotonBarra(String texto, Color fondo, Color textoColor) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(textoColor);
        btn.setBackground(fondo);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(textoColor, 1, true),
                BorderFactory.createEmptyBorder(5, 14, 5, 14)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }
}
